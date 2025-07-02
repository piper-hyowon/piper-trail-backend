package com.piper_trail.blog.command.post;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PostResponse {
  private String id;
  private String title;
  private String slug;
  private String content;
  private String category;
  private List<String> tags;
  private int viewCount;
  private Instant createdAt;
  private Instant updatedAt;
}
