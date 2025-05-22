package com.piper_trail.blog.command.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 인증 1단계 */
@Data
public class LoginRequest {
  @NotBlank private String username;
  @NotBlank private String password;
}
