package com.piper_trail.blog.shared.event;

import lombok.Getter;

/** 관리자 로그인 이벤트(보안 모니터링 용도) */
@Getter
public class AdminLoginEvent extends AbstractDomainEvent {

  private final String adminId;
  private final String username;
  private final String ipAddress;
  private final String userAgent;

  public AdminLoginEvent(String adminId, String username, String ipAddress, String userAgent) {
    super();
    this.adminId = adminId;
    this.username = username;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
  }

  @Override
  public String getAggregateId() {
    return adminId;
  }
}
