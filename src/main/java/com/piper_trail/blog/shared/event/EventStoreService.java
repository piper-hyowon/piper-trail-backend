package com.piper_trail.blog.shared.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreService {

  private final EventRepository eventRepository;

  @EventListener
  @Transactional
  public void handleDomainEvent(DomainEvent event) {
    log.debug("Storing event: {}, ID: {}", event.getEventType(), event.getEventId());

    Map<String, Object> payload = convertEventToPayload(event);

    EventDocument eventDocument =
        EventDocument.builder()
            .eventId(event.getEventId())
            .aggregateId(event.getAggregateId())
            .eventType(event.getEventType())
            .payload(payload)
            .published(false)
            .createdAt(event.getTimestamp())
            .eventSource(determineEventSource())
            .build();

    eventRepository.save(eventDocument);
  }

  /** 이벤트 발행 완료 처리 */
  @EventListener
  @Async
  public void handleEventPublished(EventPublishedEvent event) {
    log.debug("Marking event as published: {}", event.getEventId());

    eventRepository
        .findByEventId(event.getEventId())
        .ifPresent(
            eventDocument -> {
              eventDocument.setPublished(true);
              eventDocument.setPublishedAt(Instant.now());
              eventRepository.save(eventDocument);
            });
  }

  /** 도메인 이벤트를 MongoDB에 저장 가능한 Map으로 변환 */
  private Map<String, Object> convertEventToPayload(DomainEvent event) {
    Map<String, Object> payload = new HashMap<>();

    Class<?> currentClass = event.getClass();
    while (currentClass != null && !currentClass.equals(Object.class)) {
      for (Field field : currentClass.getDeclaredFields()) {
        try {
          field.setAccessible(true);
          String fieldName = field.getName();
          Object fieldValue = field.get(event);

          // static 필드나 이미 존재하는 필드 스킵
          if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
              && !payload.containsKey(fieldName)
              && fieldValue != null) {
            payload.put(fieldName, convertToStorableObject(fieldValue));
          }
        } catch (IllegalAccessException e) {
          log.warn(
              "Could not access field {} of event {}",
              field.getName(),
              event.getClass().getSimpleName(),
              e);
        }
      }
      currentClass = currentClass.getSuperclass();
    }

    return payload;
  }

  /** Object -> Map, Collection -> List 변환 */
  private Object convertToStorableObject(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof String
        || value instanceof Number
        || value instanceof Boolean
        || value instanceof Date
        || value instanceof Instant
        || value instanceof LocalDate
        || value instanceof LocalDateTime) {
      return value;
    }

    if (value instanceof Collection) {
      return ((Collection<?>) value)
          .stream().map(this::convertToStorableObject).collect(Collectors.toList());
    }

    if (value instanceof Map) {
      Map<String, Object> result = new HashMap<>();
      ((Map<?, ?>) value)
          .forEach(
              (k, v) -> {
                if (k instanceof String) {
                  result.put((String) k, convertToStorableObject(v));
                }
              });
      return result;
    }

    Map<String, Object> objectMap = new HashMap<>();
    for (Field field : value.getClass().getDeclaredFields()) {
      try {
        field.setAccessible(true);
        String fieldName = field.getName();
        Object fieldValue = field.get(value);

        if (fieldValue != null) {
          objectMap.put(fieldName, convertToStorableObject(fieldValue));
        }
      } catch (IllegalAccessException e) {
        log.warn(
            "Could not access field {} of object {}",
            field.getName(),
            value.getClass().getSimpleName(),
            e);
      }
    }

    return objectMap;
  }

  /** 관리자 액션인지 시스템 액션인지 구분 */
  private String determineEventSource() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
      return "admin:" + authentication.getName();
    }
    return "system";
  }
}
