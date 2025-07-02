package com.piper_trail.blog.query.series;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "series_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesView {
  @Id private String id; // == seriesId

  private String slug;
  private String title;
  private String titleEn;
  private String description;
  private String descriptionEn;

  private int totalCount;
  private Instant lastUpdated;

  @Builder.Default private List<PostSummary> posts = new ArrayList<>();

  private PostSummary latestPost;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostSummary {
    private String id;
    private String slug;
    private String title;
    private String titleEn;
    private int order;
    private Instant createdAt;
    private int viewCount;
  }
}
