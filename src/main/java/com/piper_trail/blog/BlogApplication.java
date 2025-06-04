package com.piper_trail.blog;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogApplication {

  public static void main(String[] args) {
    try {
      Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();

      dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

      System.out.println(System.getProperty("MONGO_URI"));

      // 필수 환경변수 검증
      validateCriticalEnvironmentVariables();

    } catch (Exception e) {
      System.err.println("환경 변수 로드 실패: " + e.getMessage());
      System.exit(1);
    }

    SpringApplication.run(BlogApplication.class, args);
  }

  // TODO: 분리
  private static void validateCriticalEnvironmentVariables() {
    String jwtSecret = System.getProperty("JWT_SECRET");

    if (jwtSecret == null) {
      throw new IllegalStateException("JWT_SECRET 404");
    }

    if (jwtSecret.length() < 32) {
      throw new IllegalStateException(
          String.format("JWT_SECRET >= 32, 현재: %d", jwtSecret.length()));
    }
  }
}
