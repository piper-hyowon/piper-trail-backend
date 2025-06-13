package com.piper_trail.blog.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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

  private String renderedContent;

  @Indexed private String category;

  @Indexed @Builder.Default private List<String> tags = new ArrayList<>();

  @Builder.Default private int viewCount = 0;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  @Builder.Default private boolean published = false;
}
