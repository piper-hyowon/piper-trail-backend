package com.piper_trail.blog.shared.cache;

import com.piper_trail.blog.shared.event.PostCreatedEvent;
import com.piper_trail.blog.shared.event.PostDeletedEvent;
import com.piper_trail.blog.shared.event.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
// 캐시 무효화
public class CacheInvalidationService {

  private final CacheManager cacheManager;

  @EventListener
  public void handlePostCreated(PostCreatedEvent event) {
    evictCache("posts");
    evictCache("categories");
    evictCache("tags");
  }

  @EventListener
  public void handlePostUpdated(PostUpdatedEvent event) {
    evictFromCache("post", event.getSlug());
    if (event.isSlugChanged()) {
      evictFromCache("post", event.getPreviousSlug());
    }

    evictCache("posts");
    evictCache("categories");
    evictCache("tags");
  }

  @EventListener
  public void handlePostDeleted(PostDeletedEvent event) {
    evictFromCache("post", event.getSlug());
    evictCache("posts");
    evictCache("categories");
    evictCache("tags");
  }

  private void evictCache(String cacheName) {
    try {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
      } else {
        log.warn("Cache not found: {}", cacheName);
      }
    } catch (Exception e) {
      log.warn("Failed to evict cache: {}", cacheName, e);
    }
  }

  private void evictFromCache(String cacheName, String key) {
    if (key == null || key.trim().isEmpty()) {
      log.warn("Invalid cache key provided for cache: {}", cacheName);
      return;
    }

    try {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.evict(key);
      } else {
        log.warn("Cache not found: {}", cacheName);
      }
    } catch (Exception e) {
      log.warn("Failed to evict key '{}' from cache: {}", key, cacheName, e);
    }
  }
}
