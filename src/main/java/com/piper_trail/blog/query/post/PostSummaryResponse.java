package com.piper_trail.blog.query.post;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PostSummaryResponse {
  private String id;
  private String title;
  private String titleEn;
  private String subtitle;
  private String subtitleEn;
  private String slug;
  private String category;
  private List<String> tags;
  private int viewCount;
  private Instant createdAt;
  private Instant updatedAt;

  private boolean isSeries;
  private SeriesInfoResponse series;

  @Data
  @Builder
  public static class SeriesInfoResponse {
    private String seriesId;
    private String seriesTitle;
    private String seriesSlug;
    private int currentOrder;
    private int totalCount;
    private boolean isLatest;
  }
}
