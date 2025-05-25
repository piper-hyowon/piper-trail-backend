package com.piper_trail.blog.query.statistics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class PostStatisticsResponse {
  private String postId;
  private String title;
  private String slug;

  private long totalViews;
  private long uniqueVisitors;
  private long returningVisitors; // 재방문
  private Map<LocalDate, Long> viewsByDay;
  private Map<String, Long> viewsByReferrer;
  private Map<String, Long> viewsByRegion;
}
