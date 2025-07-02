package com.piper_trail.blog.query.series;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SeriesResponse {
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
}
