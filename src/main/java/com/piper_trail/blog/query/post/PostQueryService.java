package com.piper_trail.blog.query.post;

import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostRepository;
import com.piper_trail.blog.shared.dto.PagedResponse;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {
  private final PostRepository postRepository;
  private final MongoTemplate mongoTemplate;

  @Cacheable(
      value = "posts_list",
      key =
          "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getAllPosts(Pageable pageable) {
    Page<Post> postPage = postRepository.findAll(pageable);
    return convertToPagedResponse(postPage);
  }

  @Cacheable(value = "post", key = "'slug:' + #slug")
  public PostDetailResponse getPostBySlug(String slug) {
    Post post =
        postRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("post", slug));

    return convertToDetailResponse(post);
  }

  @Cacheable(
      value = "posts_list",
      key =
          "'category:' + #category + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getPostsByCategory(String category, Pageable pageable) {
    System.out.println("Received pageable: " + pageable);
    System.out.println("Pageable sort: " + pageable.getSort());

    Query query = new Query(Criteria.where("category").is(category)).with(pageable);

    System.out.println("Final query: " + query);

    List<Post> posts = mongoTemplate.find(query, Post.class);
    long total =
        mongoTemplate.count(Query.query(Criteria.where("category").is(category)), Post.class);

    return createPagedResponse(posts, pageable, total);
  }

  @Cacheable(
      value = "posts_list",
      key =
          "'tag:' + #tag + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getPostsByTag(String tag, Pageable pageable) {
    Query query = new Query(Criteria.where("tags").in(tag)).with(pageable);

    List<Post> posts = mongoTemplate.find(query, Post.class);
    long total = mongoTemplate.count(Query.query(Criteria.where("tags").in(tag)), Post.class);

    return createPagedResponse(posts, pageable, total);
  }

  @Cacheable(
      value = "posts_search",
      key =
          "'search:' + #request.keyword + ':' + (#request.category ?: 'null') + ':' + (#request.tags?.toString() ?: 'null') + ':' + #request.page + ':' + #request.size + ':' + #request.sortBy + ':' + #request.sortDirection")
  public PagedResponse<PostSummaryResponse> searchPosts(PostSearchRequest request) {
    Query query = buildSearchQuery(request);
    Pageable pageable =
        PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.by(request.getSortDirection(), request.getSortBy()));

    query.with(pageable);

    List<Post> posts = mongoTemplate.find(query, Post.class);
    long total = mongoTemplate.count(query.skip(0).limit(0), Post.class);

    return createPagedResponse(posts, pageable, total);
  }

  @Cacheable(value = "metadata", key = "'categories'")
  public List<String> getAllCategories() {
    return mongoTemplate.findDistinct("category", Post.class, String.class);
  }

  @Cacheable(
      value = "posts_list",
      key =
          "'uncategorized:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
  public PagedResponse<PostSummaryResponse> getUncategorizedPosts(Pageable pageable) {
    Query query = new Query(Criteria.where("category").in(null, "")).with(pageable);

    List<Post> posts = mongoTemplate.find(query, Post.class);
    long total =
        mongoTemplate.count(Query.query(Criteria.where("category").in(null, "")), Post.class);

    return createPagedResponse(posts, pageable, total);
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

  private Query buildSearchQuery(PostSearchRequest request) {
    Query query = new Query();

    if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
      String keyword = request.getKeyword().trim();

      Criteria searchCriteria =
          new Criteria()
              .orOperator(
                  Criteria.where("title").regex(Pattern.quote(keyword), "i"),
                  Criteria.where("markdownContent").regex(Pattern.quote(keyword), "i"));

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

  private PagedResponse<PostSummaryResponse> convertToPagedResponse(Page<Post> postPage) {
    List<PostSummaryResponse> content =
        postPage.getContent().stream()
            .map(this::convertToSummaryResponse)
            .collect(Collectors.toList());

    return PagedResponse.<PostSummaryResponse>builder()
        .content(content)
        .page(postPage.getNumber())
        .size(postPage.getSize())
        .total(postPage.getTotalElements())
        .build();
  }

  private PagedResponse<PostSummaryResponse> createPagedResponse(
      List<Post> posts, Pageable pageable, long total) {
    List<PostSummaryResponse> content =
        posts.stream().map(this::convertToSummaryResponse).collect(Collectors.toList());

    return PagedResponse.<PostSummaryResponse>builder()
        .content(content)
        .page(pageable.getPageNumber())
        .size(pageable.getPageSize())
        .total(total)
        .build();
  }

  private PostSummaryResponse convertToSummaryResponse(Post post) {
    String preview = "";

    if (post.getRenderedContent() != null && !post.getRenderedContent().trim().isEmpty()) {
      String plainText = post.getRenderedContent().replaceAll("<[^>]*>", "");
      int PREVIEW_LENGTH = 200;
      preview =
          plainText.length() > PREVIEW_LENGTH
              ? plainText.substring(0, PREVIEW_LENGTH) + "..."
              : plainText;
    }

    return PostSummaryResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .slug(post.getSlug())
        .preview(preview)
        .category(post.getCategory())
        .tags(post.getTags())
        .viewCount(post.getViewCount())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }

  private PostDetailResponse convertToDetailResponse(Post post) {
    Map<String, PostDetailResponse.LinkInfo> links = buildHateoasLinks(post);

    return PostDetailResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .slug(post.getSlug())
        .content(post.getRenderedContent())
        .category(post.getCategory())
        .tags(post.getTags())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        ._links(links)
        .build();
  }

  @Cacheable(value = "post_stats", key ="'slug:' + #slug")
  public PostStatsResponse getPostStatsBySlug(String slug) {
    Post post = postRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("post", slug));

    return PostStatsResponse.builder()
            .postId(post.getId())
            .slug(post.getSlug())
            .viewCount(post.getViewCount())
            .lastUpdated(Instant.now())
            .build();
  }

  private Map<String, PostDetailResponse.LinkInfo> buildHateoasLinks(Post post) {
    Map<String, PostDetailResponse.LinkInfo> links = new HashMap<>();

    links.put(
        "self",
        PostDetailResponse.LinkInfo.builder()
            .href("/posts/" + post.getSlug())
            .method("GET")
            .title("현재 포스트")
            .build());

    if (post.getCategory() != null && !post.getCategory().trim().isEmpty()) {
      links.put(
          "category",
          PostDetailResponse.LinkInfo.builder()
              .href("/posts/category/" + post.getCategory())
              .method("GET")
              .title("같은 카테고리 포스트")
              .build());
    } else {
      links.put(
          "category",
          PostDetailResponse.LinkInfo.builder()
              .href("/posts/category/null")
              .method("GET")
              .title("미분류 포스트")
              .build());
    }

    if (post.getTags() != null && !post.getTags().isEmpty()) {
      String firstTag = post.getTags().getFirst();
      links.put(
          "tag",
          PostDetailResponse.LinkInfo.builder()
              .href("/posts/tag/" + firstTag)
              .method("GET")
              .title("'" + firstTag + "' 태그 포스트")
              .build());
    }

    links.put(
        "edit",
        PostDetailResponse.LinkInfo.builder()
            .href("/posts/" + post.getId())
            .method("PUT")
            .title("포스트 수정")
            .build());

    links.put(
        "delete",
        PostDetailResponse.LinkInfo.builder()
            .href("/posts/" + post.getId())
            .method("DELETE")
            .title("포스트 삭제")
            .build());

    return links;
  }
}
