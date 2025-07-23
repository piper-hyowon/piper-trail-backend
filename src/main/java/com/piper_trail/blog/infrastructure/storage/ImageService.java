package com.piper_trail.blog.infrastructure.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piper_trail.blog.command.post.ImageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

  private static final long MAX_FILE_SIZE = 90 * 1024 * 1024; // 90MB
  private static final List<String> ALLOWED_EXTENSIONS =
      Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");

  private final S3Client s3Client;
  private final ObjectMapper objectMapper;

  @Value("${cloudflare.r2.bucket-name}")
  private String bucketName;

  @Value("${cloudflare.r2.public-url}")
  private String publicUrl;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  public String uploadThumbnail(MultipartFile thumbnail) {
    try {
      validateImage(thumbnail);

      String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      String environment = activeProfile.equals("prod") ? "prod" : "dev";
      String basePath = String.format("%s/thumbnails/%s", environment, dateFolder);

      // TODO: 중복 제거
      String originalFilename = thumbnail.getOriginalFilename();
      String extension =
          originalFilename != null && originalFilename.contains(".")
              ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
              : ".jpg";

      String key = String.format("%s/%s%s", basePath, UUID.randomUUID(), extension);

      PutObjectRequest putRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentType(thumbnail.getContentType())
              .build();

      s3Client.putObject(
          putRequest, RequestBody.fromInputStream(thumbnail.getInputStream(), thumbnail.getSize()));

      String thumbnailUrl = publicUrl + "/" + key;

      log.info("Thumbnail uploaded successfully: {}", thumbnailUrl);
      return thumbnailUrl;

    } catch (IOException e) {
      log.error("Failed to upload thumbnail to R2", e);
      throw new RuntimeException("Failed to upload thumbnail", e);
    }
  }

  public Map<String, String> uploadImages(List<MultipartFile> images, String imageMetadataJson) {
    Map<String, String> placeholderToUrl = new HashMap<>();

    try {
      List<ImageMetadata> metadataList =
          objectMapper.readValue(imageMetadataJson, new TypeReference<List<ImageMetadata>>() {});

      // 환경별 + 날짜별 디렉터리
      String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      String environment = activeProfile.equals("prod") ? "prod" : "dev";
      String basePath = String.format("%s/%s", environment, dateFolder);

      for (int i = 0; i < images.size(); i++) {
        final int index = i;
        MultipartFile image = images.get(i);

        validateImage(image);

        ImageMetadata metadata =
            metadataList.stream().filter(m -> m.getIndex() == index).findFirst().orElse(null);

        if (metadata != null) {
          String originalFilename = image.getOriginalFilename();
          String extension =
              originalFilename != null && originalFilename.contains(".")
                  ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                  : ".jpg";

          // 환경/년/월/일/UUID.확장자
          String key = String.format("%s/%s%s", basePath, UUID.randomUUID(), extension);

          // R2에 업로드
          PutObjectRequest putRequest =
              PutObjectRequest.builder()
                  .bucket(bucketName)
                  .key(key)
                  .contentType(image.getContentType())
                  .build();

          s3Client.putObject(
              putRequest, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

          String imageUrl = publicUrl + "/" + key;
          placeholderToUrl.put(metadata.getPlaceholder(), imageUrl);
        }
      }

    } catch (IOException e) {
      log.error("Failed to upload images to R2", e);
      throw new RuntimeException("Failed to upload images", e);
    } catch (Exception e) {
      log.error("Failed to parse image metadata", e);
      throw new RuntimeException("Failed to parse image metadata", e);
    }

    return placeholderToUrl;
  }

  private void validateImage(MultipartFile image) {
    if (image.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
    }

    String originalFilename = image.getOriginalFilename();
    String extension =
        originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
            : "";

    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, png, gif, webp만 가능)");
    }
  }
}
