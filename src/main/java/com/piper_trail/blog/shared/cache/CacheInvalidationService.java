package com.piper_trail.blog.shared.cache;

import com.piper_trail.blog.shared.event.PostCreatedEvent;
import com.piper_trail.blog.shared.event.PostDeletedEvent;
import com.piper_trail.blog.shared.event.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {

  private final CacheManager cacheManager;
  private final CacheVersionService cacheVersionService;

  @EventListener
  @Order(1)
  public void handlePostCreated(PostCreatedEvent event) {
    cacheVersionService.incrementVersion();
    evictCache("posts_list");
    evictCache("posts_search");
    evictCache("metadata");
  }

  @EventListener
  @Order(1)
  public void handlePostUpdated(PostUpdatedEvent event) {
    cacheVersionService.incrementVersion();
    evictFromCache("post", "slug:" + event.getSlug());
    if (event.isSlugChanged()) {
      evictFromCache("post", "slug:" + event.getPreviousSlug());
    }
    evictCache("posts_list");
    evictCache("posts_search");
    evictCache("metadata");
  }

  @EventListener
  @Order(1)
  public void handlePostDeleted(PostDeletedEvent event) {
    cacheVersionService.incrementVersion();
    evictFromCache("post", "slug:" + event.getSlug());
    evictCache("posts_list");
    evictCache("posts_search");
    evictCache("metadata");
  }

  private void evictCache(String cacheName) {
    try {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
      } else {
        log.warn("Cache '{}' not found", cacheName);
      }
    } catch (Exception e) {
      log.warn("Failed to clear cache '{}'", cacheName, e);
    }
  }

  public void evictFromCache(String cacheName, String key) {
    if (key == null || key.trim().isEmpty()) {
      log.warn("Invalid cache key provided for cache: {}", cacheName);
      return;
    }

    try {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.evict(key);
      } else {
        log.warn("Cache '{}' not found", cacheName);
      }
    } catch (Exception e) {
      log.warn("Failed to evict key '{}' from cache '{}'", key, cacheName, e);
    }
  }
}
