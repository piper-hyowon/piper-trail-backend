package com.piper_trail.blog.infrastructure.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/wake-up")
  public String health() {
    return "I'm awake!";
  }
}
