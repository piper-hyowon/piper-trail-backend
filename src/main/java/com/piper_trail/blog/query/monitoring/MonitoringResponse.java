package com.piper_trail.blog.query.monitoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringResponse {
  private Map<String, CacheStats> cacheStatistics;
  private PerformanceMetrics performance;
  private SystemHealth systemHealth;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CacheStats {
    private double hitRate; // 퍼센트
    private double missRate; // 퍼센트
    private long requestCount;
    private long hitCount;
    private long missCount;
    private long evictionCount;
    private long estimatedSize;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PerformanceMetrics {
    private Map<String, Long> dailyViews; // 날짜별 조회수
    private List<PopularPost> topPosts;
    private long totalPosts;
    private long totalComments;
    private double spamCommentRate; // 퍼센트
    private Map<String, Long> viewsByCategory; // 카테고리별 조회수
    private RecentActivity recentActivity;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PopularPost {
    private String id;
    private String title;
    private String slug;
    private long viewCount;
    private String category;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SystemHealth {
    private String status; // UP, DOWN
    private long uptimeSeconds;
    private long memoryUsedMb;
    private long memoryMaxMb;
    private double memoryUsagePercent;
    private String mongoDbStatus;
    private int activeThreads;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RecentActivity {
    private long lastPostCreated; // timestamp
    private long lastCommentCreated;
    private long todayViews;
    private long todayComments;
  }
}
