package com.piper_trail.blog.query.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
  private String id;
  private String title;
  private String subtitle;
  private String slug;
  private String content;
  private String category;
  private List<String> tags;
  private Instant createdAt;
  private Instant updatedAt;

  private Map<String, LinkInfo> _links;

  private boolean isSeries;
  private SeriesDetailResponse series;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LinkInfo {
    private String href;
    private String method;
    private String title;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeriesDetailResponse {
    private String seriesId;
    private String seriesTitle;
    private String seriesSlug;
    private String seriesDescription;
    private int currentOrder;
    private int totalCount;
    private SeriesNavigationResponse navigation;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeriesNavigationResponse {
    private NavigationItem prev;
    private NavigationItem next;
    private List<NavigationItem> allPosts;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NavigationItem {
    private String id;
    private String title;
    private String slug;
    private int order;
    private boolean current;
  }
}
