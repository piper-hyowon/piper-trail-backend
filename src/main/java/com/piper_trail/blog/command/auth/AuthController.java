package com.piper_trail.blog.command.auth;

import com.piper_trail.blog.infrastructure.security.AuthenticationService;
import com.piper_trail.blog.shared.exception.AuthenticationException;
import com.piper_trail.blog.shared.util.ClientIpUtils;
import com.piper_trail.blog.shared.exception.TwoFactorAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
  private final AuthenticationService authenticationService;
  private final AuthCommandService authCommandService;

  /**
   * 1단계 인증: username + password
   *
   * @param request
   * @param httpRequest
   * @return 2FA 필요 여부 or QR 코드
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    log.debug("Login request: {}", request);

    String ipAddress = ClientIpUtils.extractClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    try {
      AuthResponse response = authenticationService.authenticateFirstStep(request);

      // TODO: 로그인 이력 기록
      log.info("user: {} from IP: {}", request.getUsername(), ipAddress);

      return ResponseEntity.ok(response);

    } catch (AuthenticationException e) {
      authCommandService.recordFailedLogin(
          request.getUsername(), request.getPassword(), ipAddress, userAgent);
      log.error("로그인 실패: {} {}", request.getUsername(), ipAddress);

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  /**
   * 2단계 인증: TOTP 검증, JWT 토큰 발급
   *
   * @param request
   * @param httpRequest
   * @return
   */
  @PostMapping("/two-factor")
  public ResponseEntity<AuthResponse> twoFactorAuth(
      @Valid @RequestBody TwoFactorRequest request, HttpServletRequest httpRequest) {

    try {
      AuthResponse response = authenticationService.authenticateSecondStep(request);

      String ipAddress = ClientIpUtils.extractClientIp(httpRequest);
      String userAgent = httpRequest.getHeader("User-Agent");

      authCommandService.recordSuccessfulLogin(request.getUsername(), ipAddress, userAgent);

      return ResponseEntity.ok(response);

    } catch (TwoFactorAuthenticationException e) {

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {

    try {
      AuthResponse response = authenticationService.refreshAccessToken(request);

      return ResponseEntity.ok(response);

    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
