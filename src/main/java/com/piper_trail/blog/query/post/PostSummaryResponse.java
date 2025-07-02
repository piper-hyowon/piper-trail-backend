package com.piper_trail.blog.query.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
  private SeriesInfoResponse series;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeriesInfoResponse {
    private String seriesId;
    private String seriesTitle;
    private String seriesSlug;
    private int currentOrder;
    private int totalCount;
    @JsonProperty("isLatest")
    private boolean isLatest;
  }
}
