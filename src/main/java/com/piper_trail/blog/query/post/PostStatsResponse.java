package com.piper_trail.blog.query.post;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PostStatsResponse {
  private String postId;
  private String slug;
  private long viewCount;
  private Instant lastUpdated;
}
