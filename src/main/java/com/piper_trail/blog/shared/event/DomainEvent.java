package com.piper_trail.blog.shared.event;

import java.time.Instant;

public interface DomainEvent {
  String getEventId();

  String getAggregateId();

  String getEventType();

  Instant getTimestamp();
}
