package com.piper_trail.blog.command.post;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piper_trail.blog.infrastructure.storage.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostCommandController {

  private final PostCommandService postCommandService;
  private final ImageService imageService;
  private final ObjectMapper objectMapper;

  // JSON 요청용 (이미지 미포함)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
    PostResponse response = postCommandService.createPost(request);
    URI location = URI.create("/posts/" + response.getSlug());
    return ResponseEntity.created(location).body(response);
  }

  // Multipart 요청용 (이미지 포함)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PostResponse> createPostWithImages(
      @RequestParam("title") String title,
      @RequestParam(value = "titleEn", required = false) String titleEn,
      @RequestParam("subtitle") String subtitle,
      @RequestParam(value = "subtitleEn", required = false) String subtitleEn,
      @RequestParam("markdownContent") String markdownContent,
      @RequestParam(value = "markdownContentEn", required = false) String markdownContentEn,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "tags", required = false) String tagsJson,
      @RequestParam(value = "imageMetadata", required = false) String imageMetadataJson,
      @RequestParam(value = "thumbnailUrl", required = false) String thumbnailUrl,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {

    try {
      CreatePostRequest request = new CreatePostRequest();
      request.setTitle(title);
      request.setTitleEn(titleEn);
      request.setSubtitle(subtitle);
      request.setSubtitleEn(subtitleEn);
      request.setMarkdownContent(markdownContent);
      request.setMarkdownContentEn(markdownContentEn);
      request.setCategory(category);

      if (StringUtils.hasText(tagsJson)) {
        List<String> tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        request.setTags(tags);
      } else {
        request.setTags(new ArrayList<>());
      }

      // 썸네일 처리
      if (thumbnail != null && !thumbnail.isEmpty()) {
        log.info("Processing thumbnail image: {}", thumbnail.getOriginalFilename());
        String uploadedThumbnailUrl = imageService.uploadThumbnail(thumbnail);
        request.setThumbnailUrl(uploadedThumbnailUrl);
      } else if (StringUtils.hasText(thumbnailUrl)) {
        request.setThumbnailUrl(thumbnailUrl);
      }

      if (images != null && !images.isEmpty() && StringUtils.hasText(imageMetadataJson)) {
        log.info("Processing {} images with metadata", images.size());

        // 이미지 업로드
        Map<String, String> uploadedImages = imageService.uploadImages(images, imageMetadataJson);

        // placeholder를 실제 URL로 변경
        String processedContent = markdownContent;
        String processedContentEn = markdownContentEn;

        for (Map.Entry<String, String> entry : uploadedImages.entrySet()) {
          String placeholder = entry.getKey();
          String imageUrl = entry.getValue();
          processedContent = processedContent.replace(placeholder, imageUrl);
          if (processedContentEn != null) {
            processedContentEn = processedContentEn.replace(placeholder, imageUrl);
          }
        }

        request.setMarkdownContent(processedContent);
        request.setMarkdownContentEn(processedContentEn);
      }

      PostResponse response = postCommandService.createPost(request);
      URI location = URI.create("/posts/" + response.getSlug());
      return ResponseEntity.created(location).body(response);

    } catch (Exception e) {
      log.error("Error processing multipart request", e);
      throw new RuntimeException("Failed to process request", e);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<PostResponse> updatePost(
      @PathVariable String id, @Valid @RequestBody UpdatePostRequest request) {
    PostResponse response = postCommandService.updatePost(id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable String id) {
    postCommandService.deletePost(id);
    return ResponseEntity.noContent().build();
  }
}
