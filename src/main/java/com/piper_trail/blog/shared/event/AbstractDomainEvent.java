package com.piper_trail.blog.shared.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class AbstractDomainEvent implements DomainEvent {

  private final String eventId;
  private final Instant timestamp;

  protected AbstractDomainEvent() {
    this.eventId = UUID.randomUUID().toString();
    this.timestamp = Instant.now();
  }

  @Override
  public String getEventType() {
    return this.getClass().getSimpleName();
  }
}
