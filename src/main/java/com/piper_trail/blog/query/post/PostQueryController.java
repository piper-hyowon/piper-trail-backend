package com.piper_trail.blog.query.post;

import com.piper_trail.blog.shared.config.Language;
import com.piper_trail.blog.shared.dto.PagedResponse;
import com.piper_trail.blog.shared.event.EventPublisher;
import com.piper_trail.blog.shared.event.PostViewedEvent;
import com.piper_trail.blog.shared.util.ClientIpUtils;
import com.piper_trail.blog.shared.util.ETagGenerator;
import com.piper_trail.blog.shared.util.HttpCacheUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostQueryController {
  private final PostQueryService postQueryService;
  private final ETagGenerator etagGenerator;
  private final EventPublisher eventPublisher;

  @GetMapping
  public ResponseEntity<PagedResponse<PostSummaryResponse>> getAllPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      @Language String language,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    PagedResponse<PostSummaryResponse> response = postQueryService.getAllPosts(pageable, language);

    String etag =
        etagGenerator.generateETag("posts_all", page, size, sortBy, sortDir, response.getTotal());
    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.POST_LIST_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(response, etag, HttpCacheUtils.POST_LIST_CACHE);
  }

  @GetMapping("/search")
  public ResponseEntity<PagedResponse<PostSummaryResponse>> searchPosts(
      @RequestParam String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) List<String> tags,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      @Language String language,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

    PostSearchRequest request =
        PostSearchRequest.builder()
            .keyword(keyword)
            .category(category)
            .tags(tags)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(direction)
            .build();

    PagedResponse<PostSummaryResponse> response = postQueryService.searchPosts(request, language);

    String etag =
        etagGenerator.generateETag(
            "posts_search",
            request.getKeyword(),
            request.getCategory(),
            request.getTags(),
            request.getPage(),
            request.getSize(),
            response.getTotal());

    if (etag.equals(ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.POST_LIST_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(response, etag, HttpCacheUtils.POST_LIST_CACHE);
  }

  @GetMapping("/{slug}")
  public ResponseEntity<PostDetailResponse> getPostBySlug(
      @PathVariable String slug,
      @Language String language,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    PostDetailResponse postResponse = postQueryService.getPostBySlug(slug, language);

    // HTTP 캐싱
    String etag = etagGenerator.generateETag(postResponse.getId(), postResponse.getUpdatedAt());

    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.POST_DETAIL_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(
        postResponse, etag, HttpCacheUtils.POST_DETAIL_CACHE, postResponse.getUpdatedAt());
  }

  @GetMapping("/{slug}/stats")
  public ResponseEntity<PostStatsResponse> getPostStats(
      @PathVariable String slug,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
      HttpServletRequest request,
      HttpServletResponse response) {
    String visitorId = extractOrCreateVisitorId(request, response);
    String ipAddress = ClientIpUtils.extractClientIp(request);
    String userAgent = request.getHeader("User-Agent");
    String referer = request.getHeader("Referer");

    PostStatsResponse statsResponse = postQueryService.getPostStatsBySlug(slug);

    PostViewedEvent event =
        new PostViewedEvent(statsResponse.getPostId(), visitorId, ipAddress, userAgent, referer);
    eventPublisher.publish(event);

    String etag = etagGenerator.generateETag("post_stats", slug, Instant.now());

    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.POST_STATS_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(
        statsResponse, etag, HttpCacheUtils.POST_STATS_CACHE);
  }

  private String extractOrCreateVisitorId(
      HttpServletRequest request, HttpServletResponse response) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("visitor_id".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    String newVisitorId = UUID.randomUUID().toString();

    Cookie visitorCookie = new Cookie("visitor_id", newVisitorId);
    visitorCookie.setMaxAge(60 * 60 * 24 * 365); // 1년
    visitorCookie.setPath("/");
    visitorCookie.setHttpOnly(true);
    visitorCookie.setSecure(false);

    response.addCookie(visitorCookie);

    return newVisitorId;
  }

  @GetMapping("/category/{category}")
  public ResponseEntity<PagedResponse<PostSummaryResponse>> getPostsByCategory(
      @PathVariable String category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      @Language String language,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    PagedResponse<PostSummaryResponse> response;
    if ("null".equals(category)) {
      response = postQueryService.getUncategorizedPosts(pageable, language);
    } else {
      response = postQueryService.getPostsByCategory(category, pageable, language);
    }

    String etag =
        etagGenerator.generateETag(
            "posts_category", category, page, size, sortBy, sortDir, response.getTotal());

    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.POST_LIST_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(response, etag, HttpCacheUtils.POST_LIST_CACHE);
  }

  @GetMapping("/tag/{tag}")
  public ResponseEntity<PagedResponse<PostSummaryResponse>> getPostsByTag(
      @PathVariable String tag,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      @Language String language,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    PagedResponse<PostSummaryResponse> response =
        postQueryService.getPostsByTag(tag, pageable, language);

    String etag =
        etagGenerator.generateETag(
            "posts_tag", tag, page, size, sortBy, sortDir, response.getTotal());

    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.POST_LIST_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(response, etag, HttpCacheUtils.POST_LIST_CACHE);
  }

  @GetMapping("/categories")
  public ResponseEntity<List<String>> getAllCategories(
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    List<String> categories = postQueryService.getAllCategories();

    String etag = etagGenerator.generateETag("categories", categories.size());

    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.METADATA_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(categories, etag, HttpCacheUtils.METADATA_CACHE);
  }

  @GetMapping("/tags")
  public ResponseEntity<List<String>> getAllTags(
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    List<String> tags = postQueryService.getAllTags();

    String etag = etagGenerator.generateETag("tags", tags.size());

    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.METADATA_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(tags, etag, HttpCacheUtils.METADATA_CACHE);
  }
}
