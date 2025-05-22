package com.piper_trail.blog.infrastructure.security;

import com.piper_trail.blog.shared.domain.Admin;
import com.piper_trail.blog.shared.domain.AdminRepository;
import com.piper_trail.blog.shared.exception.AuthenticationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminTotpService {
  private final AdminRepository adminRepository;
  private final TotpService totpService;

  /** TOTP 설정용 QR 코드 생성 */
  @Transactional
  public String setupTotpForAdmin(Admin admin) {
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
