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
  private String nickname;
  private String message;
  private String ipAddress;
  private String userAgent;
  @CreatedDate private Instant createdAt;

  public String getNickname() {
    return nickname != null && !nickname.trim().isEmpty() ? nickname : "익명의 방문자";
  }
}
