package com.piper_trail.blog.shared.event;

import lombok.Getter;
import java.util.List;

@Getter
public class SeriesCreatedEvent extends AbstractDomainEvent {
  private final String seriesId;
  private final String title;
  private final String slug;
  private final String description;
  private final List<String> tags;

  public SeriesCreatedEvent(
      String seriesId, String title, String slug, String description, List<String> tags) {
    super();
    this.seriesId = seriesId;
    this.title = title;
    this.slug = slug;
    this.description = description;
    this.tags = tags;
  }

  @Override
  public String getAggregateId() {
    return seriesId;
  }
}
