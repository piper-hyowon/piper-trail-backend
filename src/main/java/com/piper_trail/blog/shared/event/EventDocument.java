package com.piper_trail.blog.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDocument {

  @Id private String id;

  @Indexed(unique = true)
  private String eventId; // UUID

  @Indexed private String aggregateId;

  @Indexed private String eventType;

  private Map<String, Object> payload;

  private boolean published;

  private Instant publishedAt;

  @Indexed private Instant createdAt;

  private String requestedBy;
  private String eventSource;
  private String reason;
}
