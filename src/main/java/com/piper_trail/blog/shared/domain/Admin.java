package com.piper_trail.blog.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "admin")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

  @Id private String id;

  @Indexed(unique = true)
  private String username;

  private String password;

  private String totpSecret; // 2FA

  @Builder.Default private boolean totpEnabled = false;

  @CreatedDate private Instant createdAt;

  private Instant lastLoginAt; // 마지막 로그인 시간(해킹😱)
}
