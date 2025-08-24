package com.piper_trail.blog.shared.util;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class HttpCacheUtils {
  public static final CacheControl POST_DETAIL_CACHE =
      CacheControl.maxAge(Duration.ofDays(365)) // 1년
          .mustRevalidate(); // ETag로 검증

  // 포스트 목록: no-cache → 항상 ETag로 확인, 변경 없으면 304
  public static final CacheControl POST_LIST_CACHE =
          CacheControl.noCache();

  public static final CacheControl METADATA_CACHE =
      CacheControl.maxAge(Duration.ofDays(1)).mustRevalidate();

  public static final CacheControl POST_STATS_CACHE =
      CacheControl.maxAge(Duration.ofMinutes(1)).mustRevalidate();

  public static <T> ResponseEntity<T> createCachedResponse(
      T body, String etag, CacheControl cacheControl) {
    return createCachedResponse(body, etag, cacheControl, null);
  }

  public static <T> ResponseEntity<T> createCachedResponse(
      T body, String etag, CacheControl cacheControl, Instant lastModified) {

    ResponseEntity.BodyBuilder builder = ResponseEntity.ok().eTag(etag).cacheControl(cacheControl);

    if (lastModified != null) {
      builder.lastModified(lastModified);
    }

    return builder.body(body);
  }

  public static <T> ResponseEntity<T> createNotModifiedResponse(
      String etag, CacheControl cacheControl) {
    return ResponseEntity.status(304).eTag(etag).cacheControl(cacheControl).build();
  }

  public static boolean isETagMatched(String etag, String ifNoneMatch) {
    return Objects.equals(etag, ifNoneMatch);
  }
}
