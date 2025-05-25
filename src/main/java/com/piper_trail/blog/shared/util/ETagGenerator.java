package com.piper_trail.blog.shared.util;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class ETagGenerator {

  public String generateETag(String id, Instant lastModified) {
    String data = id + "_" + lastModified.toEpochMilli();
    return "\"" + DigestUtils.md5DigestAsHex(data.getBytes(StandardCharsets.UTF_8)) + "\"";
  }

  public String generateETag(Object... data) {
    if (data == null || data.length == 0) {
      return "\"\"";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      if (data[i] != null) {
        sb.append(data[i].toString());
        if (i < data.length - 1) {
          sb.append("_");
        }
      }
    }

    return "\"" + DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8)) + "\"";
  }
}
