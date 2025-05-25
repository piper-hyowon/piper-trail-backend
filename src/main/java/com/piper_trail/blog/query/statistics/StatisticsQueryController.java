package com.piper_trail.blog.query.statistics;

import com.piper_trail.blog.shared.util.HttpCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class StatisticsQueryController {

  private final StatisticsQueryService statisticsQueryService;

  @GetMapping("/summary")
  public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
    DashboardSummaryResponse response = statisticsQueryService.getDashboardSummary();
    return ResponseEntity.ok().cacheControl(HttpCacheUtils.METADATA_CACHE).body(response);
  }

  @GetMapping("/posts/{postId}/stats")
  public ResponseEntity<PostStatisticsResponse> getPostStatistics(@PathVariable String postId) {
    PostStatisticsResponse response = statisticsQueryService.getPostStatistics(postId);
    return ResponseEntity.ok().cacheControl(HttpCacheUtils.METADATA_CACHE).body(response);
  }
}
