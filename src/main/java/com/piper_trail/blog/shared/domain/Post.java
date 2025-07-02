package com.piper_trail.blog.shared.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
  @CompoundIndex(name = "series_order_idx", def = "{'series.seriesId': 1, 'series.order': 1}"),
  @CompoundIndex(name = "type_created_idx", def = "{'isSeries': 1, 'createdAt': -1}")
})
public class Post {
  @Id private String id;

  @Indexed
  @TextIndexed(weight = 3)
  private String title;

  @TextIndexed(weight = 2)
  private String subtitle;

  @Indexed(unique = true) // URL slug
  private String slug;

  @TextIndexed(weight = 2)
  private String markdownContent;

  @TextIndexed(weight = 3)
  private String titleEn;

  @TextIndexed(weight = 2)
  private String subtitleEn;

  @TextIndexed(weight = 2)
  private String markdownContentEn;

  @Indexed private String category;

  @Indexed @Builder.Default private List<String> tags = new ArrayList<>();

  @Builder.Default private int viewCount = 0;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  @Indexed @Builder.Default @JsonIgnore private boolean isSeries = false;

  private SeriesInfo series;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeriesInfo {
    @Indexed private String seriesId;
    private String seriesTitle;
    private int order;
  }
}
