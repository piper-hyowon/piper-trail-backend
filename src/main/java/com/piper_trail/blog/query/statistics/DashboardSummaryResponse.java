package com.piper_trail.blog.query.statistics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {
  private long totalPosts;
  private long totalViews;
  private long todayViews;
  private long thisWeekViews;
  private long uniqueVisitors;

  private List<PopularPost> topPosts;

  private Map<String, Long> topReferrers;
  private Map<LocalDate, Long> weeklyTrend;

  @Data
  @Builder
  public static class PopularPost {
    private String id;
    private String title;
    private String slug;
    private long viewCount;
  }
}
