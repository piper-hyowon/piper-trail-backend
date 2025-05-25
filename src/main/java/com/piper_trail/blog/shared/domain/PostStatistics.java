package com.piper_trail.blog.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "post_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatistics {
  @Id private String postId; // Post 랑 1:1

  // 조회수
  @Builder.Default private long totalViews = 0;
  @Builder.Default private Map<String, Long> viewsByDay = new HashMap<>();

  @Builder.Default private Map<String, Long> viewsByRegion = new HashMap<>();

  @Builder.Default private Map<String, Long> viewsByReferrer = new HashMap<>();

  @Builder.Default private long uniqueVisitors = 0;

  @Builder.Default private long returningVisitors = 0;

  @LastModifiedDate private Instant lastUpdated;
}
