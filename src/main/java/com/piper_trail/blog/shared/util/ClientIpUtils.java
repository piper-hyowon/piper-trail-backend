package com.piper_trail.blog.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientIpUtils {

  public static String extractClientIp(HttpServletRequest request) {
    String clientIp = request.getHeader("X-Forwarded-For");

    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("X-Real-IP");
    }

    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getRemoteAddr();
    }

    if (clientIp != null && clientIp.contains(",")) {
      clientIp = clientIp.split(",")[0].trim(); // 첫 번째가 실제 클라이언트
    }

    return clientIp != null ? clientIp : "unknown";
  }
}
