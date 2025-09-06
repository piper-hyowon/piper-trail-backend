package com.piper_trail.blog.query.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {

  private final MonitoringQueryService monitoringQueryService;

  @GetMapping
  public String dashboard(Model model) {
    model.addAttribute("cacheStats", monitoringQueryService.getCacheStatistics());
    model.addAttribute("performance", monitoringQueryService.getPerformanceMetrics());
    model.addAttribute("systemHealth", monitoringQueryService.getSystemHealth());

    return "monitoring/dashboard";
  }
}
