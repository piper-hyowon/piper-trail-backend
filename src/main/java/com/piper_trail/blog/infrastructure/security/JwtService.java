package com.piper_trail.blog.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

  private final JwtProperties jwtProperties;
  private SecretKey signingKey;

  private SecretKey getSigningKey() {
    if (signingKey == null) {
      byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
      signingKey = Keys.hmacShaKeyFor(keyBytes);
    }
    return signingKey;
  }

  public String generateAccessToken(String username) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(jwtProperties.getExpiration());

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiration))
        .claim("type", "access")
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String username) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(jwtProperties.getRefreshExpiration());

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiration))
        .claim("type", "refresh")
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractUsername(String token) {
    Claims claims =
        Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  public boolean validateToken(String token, String username) {
    try {
      Claims claims =
          Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token)
              .getBody();

      String tokenUsername = claims.getSubject();
      Date expiration = claims.getExpiration();

      return tokenUsername.equals(username) && expiration.after(new Date());
    } catch (JwtException e) {
      log.debug("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  public boolean isRefreshToken(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
      return "refresh".equals(claims.get("type"));
    } catch (JwtException e) {
      return false;
    }
  }
}
