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
  public CacheManager cacheManㅋager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCacheNames(
        Arrays.asList(
            "posts_list", // 포스트 목록 (카테고리, 태그별)
            "posts_search", // 포스트 검색
            "post", // 개별 포스트 상세
            "post_stats", // 포스트 통계 (조회수 등)
            "series", // 시리즈 상세
            "metadata", // 카테고리, 태그 목록
            "dashboard", // 대시보드 통계
            "comments" // 댓글 목록
            ));

    cacheManager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(cacheMaxSize)
            .expireAfterWrite(5, TimeUnit.MINUTES) // 5분 후 자동 만료
            .recordStats());

    return cacheManager;
  }
}
