package com.piper_trail.blog.query.post;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PostDetailResponse {
  private String id;
  private String title;
  private String slug;
  private String content;
  private String category;
  private List<String> tags;
  private Instant createdAt;
  private Instant updatedAt;

  private Map<String, LinkInfo> _links;

  @Data
  @Builder
  public static class LinkInfo {
    private String href;
    private String method;
    private String title;
  }
}
