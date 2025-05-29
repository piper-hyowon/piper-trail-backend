package com.piper_trail.blog;

import com.piper_trail.blog.command.postcard.PostcardRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PostcardRateLimiterTest {

  private PostcardRateLimiter rateLimiter;

  private static final int REQUESTS_PER_MINUTE = 3;
  private static final int REQUESTS_PER_HOUR = 10;

  @BeforeEach
  void setUp() {
    rateLimiter = new PostcardRateLimiter();
    rateLimiter.setRequestsPerMinute(REQUESTS_PER_MINUTE);
    rateLimiter.setRequestsPerHour(REQUESTS_PER_HOUR);
  }

  @Test
  void allowsUpToLimitRequestsPerMinute() {
    // given
    String ipAddress = "192.168.1.1";

    for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
      assertThat(rateLimiter.isAllowed(ipAddress)).isTrue();
    }
  }

  @Test
  void blocksRequestsExceedingPerMinuteLimit() {
    // given
    String ipAddress = "192.168.1.1";

    // when - limit까지 요청
    for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
      rateLimiter.isAllowed(ipAddress);
    }

    // then - 초과 요청은 거부
    assertThat(rateLimiter.isAllowed(ipAddress)).isFalse();
  }

  @Test
  void tracksSeparateLimitsPerIP() {
    // given
    String ip1 = "192.168.1.1";
    String ip2 = "192.168.1.2";

    // when - ip1이 limit까지 요청
    for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
      rateLimiter.isAllowed(ip1);
    }

    // then - ip2는 독립적으로 판단, 요청 가능
    assertThat(rateLimiter.isAllowed(ip2)).isTrue();
  }
}
