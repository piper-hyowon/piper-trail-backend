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
  private String slug;
  private String preview; // 컨텐츠 앞부분 일부
  private String category;
  private List<String> tags;
  private int viewCount;
  private Instant createdAt;
  private Instant updatedAt;
}
