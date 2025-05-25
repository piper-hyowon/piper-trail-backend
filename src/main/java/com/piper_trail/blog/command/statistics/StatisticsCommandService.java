package com.piper_trail.blog.command.statistics;

import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostStatistics;
import com.piper_trail.blog.shared.domain.VisitorTracking;
import com.piper_trail.blog.shared.event.PostViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsCommandService {

  private final MongoTemplate mongoTemplate;

  @EventListener
  @Transactional
  @CacheEvict(
      value = {"dashboard", "post-stats"},
      allEntries = true)
  public void handlePostViewedEvent(PostViewedEvent event) {
    try {
      updatePostViewCount(event.getPostId());
      updatePostStatistics(event);
    } catch (Exception e) {
      log.error("Failed to update statistics for post: {}", event.getPostId(), e);
    }
  }

  private void updatePostViewCount(String postId) {
    Update update = new Update().inc("viewCount", 1);
    mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(postId)), update, Post.class);
  }

  private void updatePostStatistics(PostViewedEvent event) {
    String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    String region = resolveRegion(event.getIpAddress());
    String referer = normalizeReferer(event.getReferer());

    Update update =
        new Update()
            .inc("totalViews", 1)
            .inc("viewsByDay." + today, 1)
            .inc("viewsByRegion." + region, 1)
            .setOnInsert("postId", event.getPostId())
            .currentDate("lastUpdated");

    if (referer != null) {
      update.inc("viewsByReferrer." + referer, 1);
    }

    boolean isUnique = isUniqueVisitor(event.getPostId(), event.getVisitorId());
    if (isUnique) {
      update.inc("uniqueVisitors", 1);
      recordVisitorTracking(event);
    } else {
      update.inc("returningVisitors", 1);
    }

    mongoTemplate.upsert(
        Query.query(Criteria.where("postId").is(event.getPostId())), update, PostStatistics.class);
  }

  private String resolveRegion(String ipAddress) {
    if (ipAddress == null || "127.0.0.1".equals(ipAddress) || "::1".equals(ipAddress)) {
      return "local";
    }
    return "unknown";
  }

  private String normalizeReferer(String referer) {
    if (referer == null || referer.trim().isEmpty()) {
      return null;
    }

    String lowerReferer = referer.toLowerCase();
    if (lowerReferer.contains("google")) return "google";
    if (lowerReferer.contains("naver")) return "naver";
    if (lowerReferer.contains("daum")) return "daum";
    if (lowerReferer.contains("github")) return "github";
    if (lowerReferer.contains("twitter")) return "twitter";
    return "direct";
  }

  private boolean isUniqueVisitor(String postId, String visitorId) {
    return !mongoTemplate.exists(
        Query.query(Criteria.where("postId").is(postId).and("visitorId").is(visitorId)),
        "visitor_tracking");
  }

  private void recordVisitorTracking(PostViewedEvent event) {
    VisitorTracking tracking =
        new VisitorTracking(
            event.getPostId(), event.getVisitorId(), event.getIpAddress(), Instant.now());

    try {
      mongoTemplate.insert(tracking);
    } catch (Exception e) {
      // 중복 방문
    }
  }
}
