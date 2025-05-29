package com.piper_trail.blog.shared.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "postcards")
public class Postcard {
  @Id private String id;

  private StampType stampType;
  @Builder.Default private String nickname = "익명의 방문자";
  private String message;
  private String ipAddress;
  private String userAgent;
  @CreatedDate private Instant createdAt;
}
