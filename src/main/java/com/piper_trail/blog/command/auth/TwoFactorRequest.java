package com.piper_trail.blog.command.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.piper_trail.blog.infrastructure.security.TotpService.TOTP_DIGITS;

/** 2단계 인증 (TOTP) */
@Data
public class TwoFactorRequest {
  @NotBlank private String username;

  @NotBlank
  @Pattern(regexp = "^[0-9]{" + TOTP_DIGITS + "}$")
  String totp;
}
