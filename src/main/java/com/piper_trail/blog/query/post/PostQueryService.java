package com.piper_trail.blog.query.post;

import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostRepository;
import com.piper_trail.blog.shared.domain.Series;
import com.piper_trail.blog.shared.domain.SeriesRepository;
import com.piper_trail.blog.shared.dto.PagedResponse;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {
  private final PostRepository postRepository;
  private final SeriesRepository seriesRepository;
  private final MongoTemplate mongoTemplate;

  @Cacheable(
      value = "posts_list",
      key =
          "'all:' + #lang + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getAllPosts(Pageable pageable, String lang) {
    Page<Post> postPage = postRepository.findAll(pageable);
    return convertToPagedResponse(postPage, lang);
  }

  @Cacheable(value = "post", key = "'slug:' + #slug + ':' + #lang")
  public PostDetailResponse getPostBySlug(String slug, String lang) {
    Post post =
        postRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("post", slug));

    boolean isEnglish = "en".equalsIgnoreCase(lang);

    PostDetailResponse.PostDetailResponseBuilder builder =
        PostDetailResponse.builder()
            .id(post.getId())
            .title(isEnglish && post.getTitleEn() != null ? post.getTitleEn() : post.getTitle())
            .subtitle(
                isEnglish && post.getSubtitleEn() != null
                    ? post.getSubtitleEn()
                    : post.getSubtitle())
            .slug(post.getSlug())
            .content(
                isEnglish && post.getMarkdownContentEn() != null
                    ? post.getMarkdownContentEn()
                    : post.getMarkdownContent())
            .category(post.getCategory())
            .tags(post.getTags())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            ._links(buildHateoasLinks(post, lang));

    // 시리즈 글인 경우 네비게이션 추가
    if (post.isSeries() && post.getSeries() != null) {
      Post.SeriesInfo seriesInfo = post.getSeries();
      seriesRepository
          .findById(seriesInfo.getSeriesId())
          .ifPresent(series -> builder.series(buildSeriesDetail(post, series)));
    }

    return builder.build();
  }

  @Cacheable(
      value = "posts_list",
      key =
          "'category:' + #category + ':' + #lang + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getPostsByCategory(
      String category, Pageable pageable, String lang) {

    if ("null".equals(category)) {
      return getUncategorizedPosts(pageable, lang);
    }

    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(Criteria.where("category").is(category)),

            // 일반글과 시리즈 최신글 분리
            Aggregation.facet()
                .and(Aggregation.match(Criteria.where("isSeries").is(false)))
                .as("regularPosts")
                .and(
                    Aggregation.match(Criteria.where("isSeries").is(true)),
                    Aggregation.sort(Sort.Direction.DESC, "series.order"),
                    Aggregation.group("series.seriesId").first("$$ROOT").as("doc"))
                .as("seriesPosts"),

            // 두 결과 통합
            Aggregation.project()
                .and(
                    AggregationExpression.from(
                        MongoExpression.create(
                            "$concatArrays: ['$regularPosts', '$seriesPosts.doc']")))
                .as("allPosts"),
            Aggregation.unwind("allPosts"),
            Aggregation.replaceRoot("allPosts"),
            Aggregation.sort(
                pageable.getSort().isSorted()
                    ? pageable.getSort()
                    : Sort.by(Sort.Direction.DESC, "createdAt")),
            Aggregation.skip(pageable.getOffset()),
            Aggregation.limit(pageable.getPageSize()));

    AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "posts", Post.class);

    List<Post> posts = results.getMappedResults();

    // 전체 카운트 (별도 쿼리)
    long regularCount =
        mongoTemplate.count(
            Query.query(Criteria.where("category").is(category).and("isSeries").is(false)),
            Post.class);
    long seriesCount =
        mongoTemplate
            .findDistinct(
                Query.query(Criteria.where("category").is(category).and("isSeries").is(true)),
                "series.seriesId",
                Post.class,
                String.class)
            .size();
    long total = regularCount + seriesCount;

    List<PostSummaryResponse> content =
        posts.stream()
            .map(post -> convertToSummaryResponseWithSeriesInfo(post, lang))
            .collect(Collectors.toList());

    System.out.println(content);

    return PagedResponse.<PostSummaryResponse>builder()
        .content(content)
        .page(pageable.getPageNumber())
        .size(pageable.getPageSize())
        .total(total)
        .build();
  }

  @Cacheable(
      value = "posts_list",
      key =
          "'tag:' + #tag + ':' + #lang + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getPostsByTag(
      String tag, Pageable pageable, String lang) {
    Query query = new Query(Criteria.where("tags").in(tag)).with(pageable);

    List<Post> posts = mongoTemplate.find(query, Post.class);
    long total = mongoTemplate.count(Query.query(Criteria.where("tags").in(tag)), Post.class);

    return createPagedResponse(posts, pageable, total, lang);
  }

  @Cacheable(
      value = "posts_search",
      key =
          "'search:' + #request.keyword + ':' + (#request.category ?: 'null') + ':' + (#request.tags?.toString() ?: 'null') + ':' + #request.page + ':' + #request.size + ':' + #request.sortBy + ':' + #request.sortDirection + ':' + #lang")
  public PagedResponse<PostSummaryResponse> searchPosts(PostSearchRequest request, String lang) {
    Query query = buildSearchQuery(request, lang);
    Pageable pageable =
        PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.by(request.getSortDirection(), request.getSortBy()));

    query.with(pageable);

    List<Post> posts = mongoTemplate.find(query, Post.class);
    long total = mongoTemplate.count(query.skip(0).limit(0), Post.class);

    return createPagedResponse(posts, pageable, total, lang);
  }

  @Cacheable(value = "metadata", key = "'categories'")
  public List<String> getAllCategories() {
    return mongoTemplate.findDistinct("category", Post.class, String.class);
  }

  @Cacheable(
      value = "posts_list",
      key =
          "'uncategorized:' + #lang + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getUncategorizedPosts(Pageable pageable, String lang) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(Criteria.where("category").in(null, "")),
            Aggregation.facet()
                .and(Aggregation.match(Criteria.where("isSeries").is(false)))
                .as("regularPosts")
                .and(
                    Aggregation.match(Criteria.where("isSeries").is(true)),
                    Aggregation.sort(Sort.Direction.DESC, "series.order"),
                    Aggregation.group("series.seriesId").first("$$ROOT").as("doc"))
                .as("seriesPosts"),
            Aggregation.project()
                .and(
                    AggregationExpression.from(
                        MongoExpression.create(
                            "$concatArrays: ['$regularPosts', '$seriesPosts.doc']")))
                .as("allPosts"),
            Aggregation.unwind("allPosts"),
            Aggregation.replaceRoot("allPosts"),
            Aggregation.sort(pageable.getSort()),
            Aggregation.skip(pageable.getOffset()),
            Aggregation.limit(pageable.getPageSize()));

    AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "posts", Post.class);

    List<Post> posts = results.getMappedResults();

    long regularCount =
        mongoTemplate.count(
            Query.query(Criteria.where("category").in(null, "").and("isSeries").is(false)),
            Post.class);
    long seriesCount =
        mongoTemplate
            .findDistinct(
                Query.query(Criteria.where("category").in(null, "").and("isSeries").is(true)),
                "series.seriesId",
                Post.class,
                String.class)
            .size();
    long total = regularCount + seriesCount;

    List<PostSummaryResponse> content =
        posts.stream()
            .map(post -> convertToSummaryResponseWithSeriesInfo(post, lang))
            .collect(Collectors.toList());

    return PagedResponse.<PostSummaryResponse>builder()
        .content(content)
        .page(pageable.getPageNumber())
        .size(pageable.getPageSize())
        .total(total)
        .build();
  }

  private PostDetailResponse.SeriesDetailResponse buildSeriesDetail(
      Post currentPost, Series series) {
    List<Post> seriesPosts = postRepository.findBySeriesId(series.getId(), Sort.by("series.order"));

    PostDetailResponse.SeriesNavigationResponse navigation =
        PostDetailResponse.SeriesNavigationResponse.builder()
            .allPosts(
                seriesPosts.stream()
                    .map(
                        p ->
                            PostDetailResponse.NavigationItem.builder()
                                .id(p.getId())
                                .title(p.getTitle())
                                .slug(p.getSlug())
                                .order(p.getSeries().getOrder())
                                .current(p.getId().equals(currentPost.getId()))
                                .build())
                    .collect(Collectors.toList()))
            .build();

    // 이전/다음 글 설정
    int currentOrder = currentPost.getSeries().getOrder();
    seriesPosts.stream()
        .filter(p -> p.getSeries().getOrder() == currentOrder - 1)
        .findFirst()
        .ifPresent(
            prev ->
                navigation.setPrev(
                    PostDetailResponse.NavigationItem.builder()
                        .id(prev.getId())
                        .title(prev.getTitle())
                        .slug(prev.getSlug())
                        .order(prev.getSeries().getOrder())
                        .build()));

    seriesPosts.stream()
        .filter(p -> p.getSeries().getOrder() == currentOrder + 1)
        .findFirst()
        .ifPresent(
            next ->
                navigation.setNext(
                    PostDetailResponse.NavigationItem.builder()
                        .id(next.getId())
                        .title(next.getTitle())
                        .slug(next.getSlug())
                        .order(next.getSeries().getOrder())
                        .build()));

    return PostDetailResponse.SeriesDetailResponse.builder()
        .seriesId(series.getId())
        .seriesTitle(series.getTitle())
        .seriesSlug(series.getSlug())
        .seriesDescription(series.getDescription())
        .currentOrder(currentOrder)
        .totalCount(series.getTotalCount())
        .navigation(navigation)
        .build();
  }

  private PostSummaryResponse convertToSummaryResponseWithSeriesInfo(Post post, String lang) {
    PostSummaryResponse response = convertToSummaryResponse(post, lang);

    if (post.isSeries() && post.getSeries() != null) {
      Post.SeriesInfo seriesInfo = post.getSeries();

      List<Post> latestPosts = postRepository.findLatestBySeriesId(seriesInfo.getSeriesId());
      boolean isLatest =
          !latestPosts.isEmpty() && latestPosts.getFirst().getId().equals(post.getId());

      Series series = seriesRepository.findById(seriesInfo.getSeriesId()).orElse(null);
      if (series != null) {
        response =
            PostSummaryResponse.builder()
                .id(response.getId())
                .title(response.getTitle())
                .titleEn(response.getTitleEn())
                .subtitle(response.getSubtitle())
                .subtitleEn(response.getSubtitleEn())
                .slug(response.getSlug())
                .category(response.getCategory())
                .tags(response.getTags())
                .viewCount(response.getViewCount())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .series(
                    PostSummaryResponse.SeriesInfoResponse.builder()
                        .seriesId(series.getId())
                        .seriesTitle(series.getTitle())
                        .seriesSlug(series.getSlug())
                        .currentOrder(post.getSeries().getOrder())
                        .totalCount(series.getTotalCount())
                        .isLatest(isLatest)
                        .build())
                .build();
      }
    }

    return response;
  }

  @Cacheable(value = "metadata", key = "'tags'")
  public List<String> getAllTags() {
    Query query = new Query();
    query.addCriteria(Criteria.where("tags").exists(true).ne(null).not().size(0));

    return mongoTemplate.findDistinct(query, "tags", Post.class, String.class).stream()
        .filter(tag -> tag != null && !tag.trim().isEmpty())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  @Cacheable(value = "post_stats", key = "'bulk:' + #slugs.hashCode()")
  public Map<String, PostStatsResponse> getBulkPostStats(List<String> slugs) {
    if (slugs == null || slugs.isEmpty()) {
      return new HashMap<>();
    }

    Query query = new Query(Criteria.where("slug").in(slugs));
    List<Post> posts = mongoTemplate.find(query, Post.class);

    Map<String, PostStatsResponse> statsMap = new HashMap<>();
    Instant now = Instant.now();

    for (Post post : posts) {
      PostStatsResponse stats =
          PostStatsResponse.builder()
              .postId(post.getId())
              .slug(post.getSlug())
              .viewCount(post.getViewCount())
              .lastUpdated(now)
              .build();
      statsMap.put(post.getSlug(), stats);
    }

    return statsMap;
  }

  private Query buildSearchQuery(PostSearchRequest request, String lang) {
    Query query = new Query();

    if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
      String keyword = request.getKeyword().trim();
      boolean isEnglish = "en".equalsIgnoreCase(lang);

      Criteria searchCriteria;
      if (isEnglish) {
        searchCriteria =
            new Criteria()
                .orOperator(
                    Criteria.where("titleEn").regex(Pattern.quote(keyword), "i"),
                    Criteria.where("markdownContentEn").regex(Pattern.quote(keyword), "i"),
                    Criteria.where("title").regex(Pattern.quote(keyword), "i"),
                    Criteria.where("markdownContent").regex(Pattern.quote(keyword), "i"));
      } else {
        searchCriteria =
            new Criteria()
                .orOperator(
                    Criteria.where("title").regex(Pattern.quote(keyword), "i"),
                    Criteria.where("markdownContent").regex(Pattern.quote(keyword), "i"));
      }

      query.addCriteria(searchCriteria);
    }

    if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
      query.addCriteria(Criteria.where("category").is(request.getCategory()));
    }

    if (request.getTags() != null && !request.getTags().isEmpty()) {
      query.addCriteria(Criteria.where("tags").in(request.getTags()));
    }

    return query;
  }

  private PagedResponse<PostSummaryResponse> convertToPagedResponse(
      Page<Post> postPage, String lang) {
    List<PostSummaryResponse> content =
        postPage.getContent().stream()
            .map(post -> convertToSummaryResponse(post, lang))
            .collect(Collectors.toList());

    return PagedResponse.<PostSummaryResponse>builder()
        .content(content)
        .page(postPage.getNumber())
        .size(postPage.getSize())
        .total(postPage.getTotalElements())
        .build();
  }

  private PagedResponse<PostSummaryResponse> createPagedResponse(
      List<Post> posts, Pageable pageable, long total, String lang) {
    List<PostSummaryResponse> content =
        posts.stream()
            .map(post -> convertToSummaryResponse(post, lang))
            .collect(Collectors.toList());

    return PagedResponse.<PostSummaryResponse>builder()
        .content(content)
        .page(pageable.getPageNumber())
        .size(pageable.getPageSize())
        .total(total)
        .build();
  }

  private PostSummaryResponse convertToSummaryResponse(Post post, String lang) {
    boolean isEnglish = "en".equalsIgnoreCase(lang);

    String title = isEnglish && post.getTitleEn() != null ? post.getTitleEn() : post.getTitle();

    String subtitle =
        isEnglish && post.getSubtitleEn() != null ? post.getSubtitleEn() : post.getSubtitle();

    return PostSummaryResponse.builder()
        .id(post.getId())
        .title(title)
        .subtitle(subtitle)
        .slug(post.getSlug())
        .category(post.getCategory())
        .tags(post.getTags())
        .viewCount(post.getViewCount())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }

  private PostDetailResponse convertToDetailResponse(Post post, String lang) {
    boolean isEnglish = "en".equalsIgnoreCase(lang);

    String title = isEnglish && post.getTitleEn() != null ? post.getTitleEn() : post.getTitle();
    String subtitle =
        isEnglish && post.getSubtitleEn() != null ? post.getSubtitleEn() : post.getSubtitle();
    String content =
        isEnglish && post.getMarkdownContentEn() != null
            ? post.getMarkdownContentEn()
            : post.getMarkdownContent();

    Map<String, PostDetailResponse.LinkInfo> links = buildHateoasLinks(post, lang);

    PostDetailResponse.PostDetailResponseBuilder builder =
        PostDetailResponse.builder()
            .id(post.getId())
            .title(title)
            .subtitle(subtitle)
            .slug(post.getSlug())
            .content(content)
            .category(post.getCategory())
            .tags(post.getTags())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            ._links(links);

    return builder.build();
  }

  @Cacheable(value = "post_stats", key = "'slug:' + #slug")
  public PostStatsResponse getPostStatsBySlug(String slug) {
    Post post =
        postRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("post", slug));

    return PostStatsResponse.builder()
        .postId(post.getId())
        .slug(post.getSlug())
        .viewCount(post.getViewCount())
        .lastUpdated(Instant.now())
        .build();
  }

  private Map<String, PostDetailResponse.LinkInfo> buildHateoasLinks(Post post, String lang) {
    Map<String, PostDetailResponse.LinkInfo> links = new HashMap<>();
    boolean isEnglish = "en".equalsIgnoreCase(lang);

    links.put(
        "self",
        PostDetailResponse.LinkInfo.builder()
            .href("/posts/" + post.getSlug())
            .method("GET")
            .title(isEnglish ? "Current Post" : "현재 포스트")
            .build());

    if (post.getCategory() != null && !post.getCategory().trim().isEmpty()) {
      links.put(
          "category",
          PostDetailResponse.LinkInfo.builder()
              .href("/posts/category/" + post.getCategory())
              .method("GET")
              .title(isEnglish ? "Same Category Posts" : "같은 카테고리 포스트")
              .build());
    } else {
      links.put(
          "category",
          PostDetailResponse.LinkInfo.builder()
              .href("/posts/category/null")
              .method("GET")
              .title(isEnglish ? "Uncategorized Posts" : "미분류 포스트")
              .build());
    }

    if (post.getTags() != null && !post.getTags().isEmpty()) {
      String firstTag = post.getTags().getFirst();
      links.put(
          "tag",
          PostDetailResponse.LinkInfo.builder()
              .href("/posts/tag/" + firstTag)
              .method("GET")
              .title(
                  isEnglish ? "Posts tagged with '" + firstTag + "'" : "'" + firstTag + "' 태그 포스트")
              .build());
    }

    links.put(
        "edit",
        PostDetailResponse.LinkInfo.builder()
            .href("/posts/" + post.getId())
            .method("PUT")
            .title(isEnglish ? "Edit Post" : "포스트 수정")
            .build());

    links.put(
        "delete",
        PostDetailResponse.LinkInfo.builder()
            .href("/posts/" + post.getId())
            .method("DELETE")
            .title(isEnglish ? "Delete Post" : "포스트 삭제")
            .build());

    return links;
  }

  private PostSummaryResponse convertToSummaryResponse(Post post) {
    return convertToSummaryResponse(post, "ko");
  }
}
