package com.piper_trail.blog.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "series")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Series {
  @Id private String id;

  @Indexed(unique = true)
  private String slug;

  private String title;
  private String description;

  private String titleEn;
  private String descriptionEn;

  @Builder.Default private int totalCount = 0;

  @Builder.Default private List<String> tags = new ArrayList<>();

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;
}
