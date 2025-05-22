package com.piper_trail.blog.infrastructure.security;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TotpService {
  private static final int TOTP_PERIOD_SECONDS = 30;
  public static final int TOTP_DIGITS = 6;

  private final SecretGenerator secretGenerator;
  private final CodeGenerator codeGenerator;
  private final CodeVerifier codeVerifier;
  private final QrGenerator qrGenerator;

  public TotpService() {
    this.secretGenerator = new DefaultSecretGenerator();
    this.codeGenerator = new DefaultCodeGenerator();
    this.codeVerifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());
    this.qrGenerator = new ZxingPngQrGenerator();
  }

  public String generateSecret() {
    return secretGenerator.generate();
  }

  /**
   * QR 코드 데이터 URL 생성(초기 설정용)
   *
   * @param username 관리자 이름
   * @param secret TOTP 시크릿
   * @return QR 코드 이미지 데이터 URL
   * @throws QrGenerationException QR 코드 생성 실패
   */
  public String getQrCodeDataUrl(String username, String secret) throws QrGenerationException {
    QrData data =
        new QrData.Builder()
            .label(username) // OTP 앱에서 표시될 계정명
            .secret(secret)
            .issuer("Piper Trail Blog")
            .algorithm(HashingAlgorithm.SHA1)
            .digits(TOTP_DIGITS)
            .period(TOTP_PERIOD_SECONDS)
            .build();
    String base64Image = java.util.Base64.getEncoder().encodeToString(qrGenerator.generate(data));

    return "data:image/png;base64," + base64Image;
  }

  public boolean verifyCode(String secret, String code) {
    if (secret == null || code == null) {
      return false;
    }

    return codeVerifier.isValidCode(secret, code);
  }
}
