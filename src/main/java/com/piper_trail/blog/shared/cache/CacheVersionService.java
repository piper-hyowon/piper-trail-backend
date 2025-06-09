package com.piper_trail.blog.shared.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class CacheVersionService {
  private final AtomicLong version = new AtomicLong(System.currentTimeMillis());

  public long getVersion() {
    return version.get();
  }

  public void incrementVersion() {
    version.incrementAndGet();
  }
}
