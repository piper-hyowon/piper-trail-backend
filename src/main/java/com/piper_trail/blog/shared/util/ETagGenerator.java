package com.piper_trail.blog.shared.util;

import com.piper_trail.blog.shared.cache.CacheVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ETagGenerator {
  private final CacheVersionService cacheVersionService;

  public String generateETag(String id, Instant lastModified) {
    long version = cacheVersionService.getVersion();
    long timestamp = (lastModified != null) ? lastModified.toEpochMilli() : 0L;
    String data = id + "_" + timestamp + "_" + version;

    return "\"" + DigestUtils.md5DigestAsHex(data.getBytes(StandardCharsets.UTF_8)) + "\"";
  }

  public String generateETag(Object... data) {
    if (data == null || data.length == 0) {
      return "\"empty\"";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      if (data[i] != null) {
        sb.append(data[i].toString());
        if (i < data.length - 1) {
          sb.append("_");
        }
      } else {
        sb.append("null");
        if (i < data.length - 1) {
          sb.append("_");
        }
      }
    }
    sb.append("_").append(cacheVersionService.getVersion());

    return "\"" + DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8)) + "\"";
  }
}
