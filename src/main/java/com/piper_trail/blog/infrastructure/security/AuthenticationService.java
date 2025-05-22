package com.piper_trail.blog.infrastructure.security;

import com.piper_trail.blog.command.auth.AuthResponse;
import com.piper_trail.blog.command.auth.LoginRequest;
import com.piper_trail.blog.command.auth.RefreshTokenRequest;
import com.piper_trail.blog.command.auth.TwoFactorRequest;
import com.piper_trail.blog.shared.domain.Admin;
import com.piper_trail.blog.shared.domain.AdminRepository;
import com.piper_trail.blog.shared.exception.AuthenticationException;
import com.piper_trail.blog.shared.exception.TwoFactorAuthenticationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final TotpService totpService;
  private final JwtProperties jwtProperties;

  // 1단계 인증: username, password
  public AuthResponse authenticateFirstStep(LoginRequest request) {
    Admin admin =
        adminRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

    log.debug("admin{}", admin);
    if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
      log.debug("비밀번호 틀림!!!");
      throw new AuthenticationException("Invalid username or password");
    }

    // TOTP 설정 완료 or 설정중(첫 TOTP인증대기 상태)
    if (admin.getTotpSecret() != null) {
      log.debug("TotpSecret: {}", admin.getTotpSecret());
      return AuthResponse.builder().requiresTwoFactor(true).build();
    }

    // TOTP 가 설정되지 않은 경우 -> QR 코드 생성
    String qrCodeDataUrl = generateQrCodeForTotp(admin);
    return AuthResponse.builder().qrCodeDataUrl(qrCodeDataUrl).build();
  }

  // 2단계 인증: TOTP 검증 및 토큰 생성
  @Transactional
  public AuthResponse authenticateSecondStep(TwoFactorRequest request) {
    Admin admin =
        adminRepository
            .findByUsername(request.getUsername())
            .orElseThrow(
                () -> new TwoFactorAuthenticationException("Invalid username or password"));

    if (admin.getTotpSecret() == null) {
      throw new TwoFactorAuthenticationException("TOTP 설정 필요");
    }

    if (!totpService.verifyCode(admin.getTotpSecret(), request.getTotp())) {
      throw new TwoFactorAuthenticationException("Invalid TOTP");
    }

    // TOTP 검증 성공 시 활성화
    if (!admin.isTotpEnabled()) {
      admin.setTotpEnabled(true);
      adminRepository.save(admin);
    }

    String accessToken = jwtService.generateAccessToken(admin.getUsername());
    String refreshToken = jwtService.generateRefreshToken(admin.getUsername());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresInSeconds(jwtProperties.getExpiration() / 1000)
        .build();
  }

  @Transactional(readOnly = true)
  public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
    if (!jwtService.isRefreshToken(request.getRefreshToken())) {
      throw new AuthenticationException("Invalid refresh token");
    }

    String username;
    try {
      username = jwtService.extractUsername(request.getRefreshToken());
    } catch (Exception e) {
      throw new AuthenticationException("Invalid refresh token");
    }

    if (!adminRepository.findByUsername(username).isPresent()) {
      throw new AuthenticationException("Invalid refresh token");
    }

    String newAccessToken = jwtService.generateAccessToken(username);

    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .accessTokenExpiresInSeconds(jwtProperties.getExpiration() / 1000)
        .build();
  }

  /** TOTP 설정용 QR 코드 생성 */
  @Transactional
  public String generateQrCodeForTotp(Admin admin) {
    String secret = admin.getTotpSecret();
    if (secret == null) {
      secret = totpService.generateSecret();
      admin.setTotpSecret(secret);
      adminRepository.save(admin);
    }

    try {
      return totpService.getQrCodeDataUrl(admin.getUsername(), secret);
    } catch (QrGenerationException e) {
      throw new AuthenticationException("QR 코드 생성 실패");
    }
  }
}
