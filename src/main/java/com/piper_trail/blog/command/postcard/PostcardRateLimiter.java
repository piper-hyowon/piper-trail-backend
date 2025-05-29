package com.piper_trail.blog.command.postcard;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConfigurationProperties(prefix = "postcard.rate-limit")
@Data
public class PostcardRateLimiter {

  // <IP주소, 요청 시간>
  private final Map<String, List<Instant>> requestHistory = new ConcurrentHashMap<>();

  // 설정값으로 관리
  private int requestsPerMinute;
  private int requestsPerHour;

  public boolean isAllowed(String ipAddress) {
    List<Instant> requests = requestHistory.computeIfAbsent(ipAddress, k -> new ArrayList<>());
    Instant now = Instant.now();

    // 1시간 이전 요청들 제거하고나서 체크
    Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
    requests.removeIf(time -> time.isBefore(oneHourAgo));
    if (requests.size() >= requestsPerHour) {
      return false;
    }

    // 1분 제한 체크
    Instant oneMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
    long recentRequests = requests.stream().filter(time -> time.isAfter(oneMinuteAgo)).count();
    if (recentRequests >= requestsPerMinute) {
      return false;
    }

    // 요청 가능하면 현재 시간 추가
    requests.add(now);
    return true;
  }
}
