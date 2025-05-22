package com.piper_trail.blog.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
  private String secret;
  private long expiration = 3_600_000L; // 1시간
  private long refreshExpiration = 259_200_000L; // 3일
}
