package com.piper_trail.blog.command.postcard;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
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
@Slf4j
public class PostcardRateLimiter {

  private final Map<String, List<Instant>> requestHistory = new ConcurrentHashMap<>();

  private int requestsPerMinute;
  private int requestsPerHour;

  public boolean isAllowed(String ipAddress) {
    List<Instant> requests = requestHistory.computeIfAbsent(ipAddress, k -> new ArrayList<>());
    Instant now = Instant.now();

    synchronized (requests) {
      // 1시간 이전 요청들 제거
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

  /** 매일 자정에 2시간 이상 된 오래된 기록 정리 */
  @Scheduled(cron = "0 0 0 * * *") // 매일 자정
  public void cleanupOldEntries() {
    log.info("Cleaning up old rate limiter entries");
    Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);

    requestHistory
        .entrySet()
        .removeIf(
            entry -> {
              List<Instant> requests = entry.getValue();
              synchronized (requests) {
                requests.removeIf(time -> time.isBefore(twoHoursAgo));
                return requests.isEmpty(); // 빈 리스트면 엔트리 자체를 제거
              }
            });

    log.info("Rate limiter cleanup completed. Remaining IPs: {}", requestHistory.size());
  }
}
