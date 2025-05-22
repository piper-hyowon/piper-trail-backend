package com.piper_trail.blog.shared.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
  private final ApplicationEventPublisher applicationEventPublisher;

  public void publish(DomainEvent event) {
    log.info("Publishing event {}, ID: {}", event.getEventType(), event.getEventId());
    applicationEventPublisher.publishEvent(event);
  }
}
