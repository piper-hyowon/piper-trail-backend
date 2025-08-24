package com.piper_trail.blog.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
  @Value("${app.cache.max-size:1000}")
  private int cacheMaxSize;

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    // TODO :미사용 캐시 이름 삭제
    cacheManager.setCacheNames(
        Arrays.asList(
            "posts",
            "posts_list",
            "posts_search",
            "post",
            "post_stats",
            "series",
            "tags",
            "categories",
            "metadata",
            "category-stats",
            "statistics",
            "comments",
            "search",
            "dashboard",
            "post-stats"));

    cacheManager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(cacheMaxSize)
            .expireAfterWrite(5, TimeUnit.MINUTES) // 5분 후 자동 만료
            .recordStats());

    return cacheManager;
  }
}
