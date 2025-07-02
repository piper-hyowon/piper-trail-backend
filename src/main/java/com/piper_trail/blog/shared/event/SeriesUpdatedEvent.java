package com.piper_trail.blog.shared.event;

import lombok.Getter;

@Getter
public class SeriesUpdatedEvent extends AbstractDomainEvent {
  private final String seriesId;
  private final String title;
  private final String slug;
  private final int totalCount;

  public SeriesUpdatedEvent(String seriesId, String title, String slug, int totalCount) {
    super();
    this.seriesId = seriesId;
    this.title = title;
    this.slug = slug;
    this.totalCount = totalCount;
  }

  @Override
  public String getAggregateId() {
    return seriesId;
  }
}
