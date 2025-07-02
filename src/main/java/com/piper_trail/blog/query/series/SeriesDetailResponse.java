package com.piper_trail.blog.query.series;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SeriesDetailResponse {
  private String id;
  private String slug;
  private String title;
  private String titleEn;
  private String description;
  private String descriptionEn;
  private int totalCount;
  private List<String> tags;
  private Instant createdAt;
  private Instant lastUpdated;

  private List<SeriesPostItem> posts;

  @Data
  @Builder
  public static class SeriesPostItem {
    private String id;
    private String slug;
    private String title;
    private String titleEn;
    private String subtitle;
    private String subtitleEn;
    private int order;
    private Instant createdAt;
    private int viewCount;
  }
}
