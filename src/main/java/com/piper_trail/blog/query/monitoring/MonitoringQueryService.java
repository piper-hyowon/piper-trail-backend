package com.piper_trail.blog.query.monitoring;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.piper_trail.blog.shared.domain.Comment;
import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MonitoringQueryService {

  private final CacheManager cacheManager;
  private final MongoTemplate mongoTemplate;

  public Map<String, MonitoringResponse.CacheStats> getCacheStatistics() {
    Map<String, MonitoringResponse.CacheStats> stats = new HashMap<>();

    cacheManager
        .getCacheNames()
        .forEach(
            cacheName -> {
              org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
              if (springCache instanceof CaffeineCache) {
                Cache<Object, Object> nativeCache = ((CaffeineCache) springCache).getNativeCache();
                CacheStats cacheStats = nativeCache.stats();

                stats.put(
                    cacheName,
                    MonitoringResponse.CacheStats.builder()
                        .hitRate(Math.round(cacheStats.hitRate() * 10000) / 100.0)
                        .missRate(Math.round(cacheStats.missRate() * 10000) / 100.0)
                        .requestCount(cacheStats.requestCount())
                        .hitCount(cacheStats.hitCount())
                        .missCount(cacheStats.missCount())
                        .evictionCount(cacheStats.evictionCount())
                        .estimatedSize(nativeCache.estimatedSize())
                        .build());
              }
            });

    return stats;
  }

  public MonitoringResponse.PerformanceMetrics getPerformanceMetrics() {
    // 최근 7일 일별 조회수
    Map<String, Long> dailyViews = calculateDailyViews(7);

    // TOP 5 인기 포스트
    List<MonitoringResponse.PopularPost> topPosts = getTopViewedPosts(5);

    // 전체 통계
    long totalPosts = mongoTemplate.count(new Query(), Post.class);
    long totalComments = mongoTemplate.count(new Query(), Comment.class);

    // 스팸 댓글 비율
    long spamComments =
        mongoTemplate.count(
            Query.query(
                Criteria.where("needsReview")
                    .is(true)
                    .orOperator(Criteria.where("hidden").is(true))),
            Comment.class);

    double spamRate =
        totalComments > 0 ? Math.round(spamComments * 10000.0 / totalComments) / 100.0 : 0.0;

    return MonitoringResponse.PerformanceMetrics.builder()
        .dailyViews(dailyViews)
        .topPosts(topPosts)
        .totalPosts(totalPosts)
        .totalComments(totalComments)
        .spamCommentRate(spamRate)
        .build();
  }

  public MonitoringResponse.SystemHealth getSystemHealth() {
    Runtime runtime = Runtime.getRuntime();
    long uptime = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();

    // MongoDB 연결 상태 체크
    String mongoStatus;
    try {
      mongoTemplate.getDb().runCommand(new Document("ping", 1));
      mongoStatus = "Connected";
    } catch (Exception e) {
      mongoStatus = "Disconnected";
      log.error("MongoDB connection check failed", e);
    }

    return MonitoringResponse.SystemHealth.builder()
        .status("UP")
        .uptimeSeconds(uptime / 1000)
        .memoryUsedMb((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024)
        .memoryMaxMb(runtime.maxMemory() / 1024 / 1024)
        .memoryUsagePercent(
            Math.round(
                    ((runtime.totalMemory() - runtime.freeMemory()) * 10000.0)
                        / runtime.maxMemory())
                / 100.0)
        .mongoDbStatus(mongoStatus)
        .build();
  }

  private Map<String, Long> calculateDailyViews(int days) {
    Map<String, Long> dailyViews = new LinkedHashMap<>();
    LocalDate today = LocalDate.now();

    for (int i = days - 1; i >= 0; i--) {
      String date = today.minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);

      Aggregation aggregation =
          Aggregation.newAggregation(Aggregation.group().sum("viewsByDay." + date).as("total"));

      AggregationResults<Document> results =
          mongoTemplate.aggregate(aggregation, PostStatistics.class, Document.class);

      Long views = 0L;
      if (!results.getMappedResults().isEmpty()) {
        Object total = results.getMappedResults().get(0).get("total");
        views = total != null ? ((Number) total).longValue() : 0L;
      }

      dailyViews.put(date, views);
    }

    return dailyViews;
  }

  private List<MonitoringResponse.PopularPost> getTopViewedPosts(int limit) {
    Query query = new Query().with(Sort.by(Sort.Direction.DESC, "viewCount")).limit(limit);

    return mongoTemplate.find(query, Post.class).stream()
        .map(
            post ->
                MonitoringResponse.PopularPost.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .slug(post.getSlug())
                    .viewCount(post.getViewCount())
                    .build())
        .collect(Collectors.toList());
  }
}
