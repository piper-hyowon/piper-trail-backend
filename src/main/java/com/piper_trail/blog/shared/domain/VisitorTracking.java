package com.piper_trail.blog.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "visitor_tracking")
@CompoundIndex(def = "{'postId': 1, 'visitorId': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorTracking {
  @Id private String id;
  private String postId;
  private String visitorId;
  private String ipAddress;
  private Instant visitedAt;

  public VisitorTracking(String postId, String visitorId, String ipAddress, Instant visitedAt) {
    this.postId = postId;
    this.visitorId = visitorId;
    this.ipAddress = ipAddress;
    this.visitedAt = visitedAt;
  }
}
