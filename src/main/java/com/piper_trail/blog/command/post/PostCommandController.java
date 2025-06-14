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

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<PostResponse> createPost(
      @Valid @RequestBody(required = false) CreatePostRequest jsonRequest,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "titleEn", required = false) String titleEn,
      @RequestParam(value = "subtitle", required = false) String subtitle,
      @RequestParam(value = "subtitleEn", required = false) String subtitleEn,
      @RequestParam(value = "markdownContent", required = false) String markdownContent,
      @RequestParam(value = "markdownContentEn", required = false) String markdownContentEn,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "tags", required = false) String tagsJson,
      @RequestParam(value = "imageMetadata", required = false) String imageMetadataJson,
      @RequestPart(value = "images", required = false) List<MultipartFile> images) {

    PostResponse response;

    // JSON 요청(이미지 미포함)
    if (jsonRequest != null) {
      response = postCommandService.createPost(jsonRequest);
    }
    // Multipart 요청(이미지 포함)
    else {
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
          List<String> tags =
              objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
          request.setTags(tags);
        } else {
          request.setTags(new ArrayList<>());
        }

        // 이미지가 있는 경우 처리
        if (images != null && !images.isEmpty() && StringUtils.hasText(imageMetadataJson)) {
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

        response = postCommandService.createPost(request);

      } catch (Exception e) {
        log.error("Error processing multipart request", e);
        throw new RuntimeException("Failed to process request", e);
      }
    }

    URI location = URI.create("/posts/" + response.getSlug());
    return ResponseEntity.created(location).body(response);
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
