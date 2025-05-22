package com.piper_trail.blog.shared.event;

import lombok.Getter;

// 이벤트 발행 완료 이벤트
@Getter
public class EventPublishedEvent {
  private final String eventId;

  public EventPublishedEvent(String eventId) {
    this.eventId = eventId;
  }
}
