package com.piper_trail.blog.shared.event;

import lombok.Getter;
import java.time.Instant;

@Getter
public class SeriesPostAddedEvent extends AbstractDomainEvent {
  private final String postId;
  private final String seriesId;
  private final String title;
  private final String slug;
  private final int order;
  private final int viewCount;
  private final Instant createdAt;

  public SeriesPostAddedEvent(
      String postId,
      String seriesId,
      String title,
      String slug,
      int order,
      int viewCount,
      Instant createdAt) {
    super();
    this.postId = postId;
    this.seriesId = seriesId;
    this.title = title;
    this.slug = slug;
    this.order = order;
    this.viewCount = viewCount;
    this.createdAt = createdAt;
  }

  @Override
  public String getAggregateId() {
    return seriesId;
  }
}
