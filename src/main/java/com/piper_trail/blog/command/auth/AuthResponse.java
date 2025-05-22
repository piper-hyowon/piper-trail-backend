package com.piper_trail.blog.command.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
  private boolean requiresTwoFactor;
  @Nullable private String accessToken; // 2단계 인증 완료시 포함
  @Nullable private String refreshToken; // 2단계 인증 완료시 포함
  @Nullable private String qrCodeDataUrl; // TOTP 설정 필요시(최초1 회)
  @Nullable private Long accessTokenExpiresInSeconds;
}
