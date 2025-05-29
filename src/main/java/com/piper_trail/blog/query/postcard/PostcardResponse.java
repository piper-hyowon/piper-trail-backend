package com.piper_trail.blog.query.postcard;

import com.piper_trail.blog.shared.domain.StampType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PostcardResponse {
  private String id;
  private StampType stampType;
  private String nickname;
  private String message;
  private Instant createdAt;
}
