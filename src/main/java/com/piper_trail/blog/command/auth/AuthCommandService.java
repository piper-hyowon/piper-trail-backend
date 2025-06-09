package com.piper_trail.blog.command.auth;

import com.piper_trail.blog.shared.domain.Admin;
import com.piper_trail.blog.shared.domain.AdminRepository;
import com.piper_trail.blog.shared.event.AdminLoginAttemptEvent;
import com.piper_trail.blog.shared.event.EventPublisher;
import com.piper_trail.blog.shared.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthCommandService {

  private final AdminRepository adminRepository;
  private final EventPublisher eventPublisher;

  // 로그인 성공 이벤트 발행
  @Transactional
  public void recordSuccessfulLogin(String username, String ipAddress, String userAgent) {
    Admin admin =
        adminRepository
            .findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("Invalid username"));

    eventPublisher.publish(
        AdminLoginAttemptEvent.success(admin.getId(), username, ipAddress, userAgent));
  }

  // 로그인 실패 기록
  @Transactional
  public void recordFailedLogin(
      String username, String attemptedPassword, String ipAddress, String userAgent) {
    eventPublisher.publish(
        AdminLoginAttemptEvent.failure(username, attemptedPassword, ipAddress, userAgent));
  }
}
