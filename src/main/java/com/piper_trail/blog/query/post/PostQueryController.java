package com.piper_trail.blog.query.post;

import com.piper_trail.blog.shared.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostQueryController {
  private final PostQueryService postQueryService;

  @GetMapping
  public ResponseEntity<PagedResponse<PostSummaryResponse>> getAllPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    PagedResponse<PostSummaryResponse> response = postQueryService.getAllPosts(pageable);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/search")
  public ResponseEntity<PagedResponse<PostSummaryResponse>> searchPosts(
      @RequestParam String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) List<String> tags,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

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

    PagedResponse<PostSummaryResponse> response = postQueryService.searchPosts(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{slug}")
  public ResponseEntity<PostDetailResponse> getPost(@PathVariable String slug) {
    PostDetailResponse response = postQueryService.getPostBySlug(slug);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/category/{category}")
  public ResponseEntity<PagedResponse<PostSummaryResponse>> getAllPostsByCategory(
      @PathVariable String category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    PagedResponse<PostSummaryResponse> response;
    if ("null".equals(category)) {
      response = postQueryService.getUncategorizedPosts(pageable);
    } else {
      response = postQueryService.getPostsByCategory(category, pageable);
    }

    return ResponseEntity.ok(response);
  }

  @GetMapping("/tag/{tag}")
  public ResponseEntity<PagedResponse<PostSummaryResponse>> getPostsByTag(
      @PathVariable String tag,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    PagedResponse<PostSummaryResponse> response = postQueryService.getPostsByTag(tag, pageable);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/categories")
  public ResponseEntity<List<String>> getAllCategories() {
    List<String> categories = postQueryService.getAllCategories();
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/tags")
  public ResponseEntity<List<String>> getAllTags() {
    List<String> tags = postQueryService.getAllTags();
    return ResponseEntity.ok(tags);
  }
}
