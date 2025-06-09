package com.piper_trail.blog.shared.event;

import lombok.Getter;

/** 관리자 로그인 시도 이벤트 */
@Getter
public class AdminLoginAttemptEvent extends AbstractDomainEvent {

  private final String username;
  private final String ipAddress;
  private final String userAgent;
  private final boolean success;
  private final String attemptedPassword; // 실패 시에만
  private final String adminId; // 성공 시에만

  private AdminLoginAttemptEvent(
      String adminId,
      String username,
      String attemptedPassword,
      String ipAddress,
      String userAgent,
      boolean success) {
    super();
    this.adminId = adminId;
    this.username = username;
    this.attemptedPassword = attemptedPassword;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.success = success;
  }

  public static AdminLoginAttemptEvent success(
      String adminId, String username, String ipAddress, String userAgent) {
    return new AdminLoginAttemptEvent(adminId, username, null, ipAddress, userAgent, true);
  }

  public static AdminLoginAttemptEvent failure(
      String username, String attemptedPassword, String ipAddress, String userAgent) {
    return new AdminLoginAttemptEvent(
        null, username, attemptedPassword, ipAddress, userAgent, false);
  }

  @Override
  public String getAggregateId() {
    return adminId != null ? adminId : "failed:" + username;
  }
}
