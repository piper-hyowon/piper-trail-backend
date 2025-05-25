package com.piper_trail.blog.query.statistics;

import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostStatistics;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ObjectOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsQueryService {
  private final MongoTemplate mongoTemplate;

  @Cacheable(value = "dashboard", key = "'summary'")
  public DashboardSummaryResponse getDashboardSummary() {
    long totalPosts = mongoTemplate.count(new Query(), Post.class);

    long totalViews = calculateTotalViews();
    long todayViews = calculateTodayViews();
    long thisWeekViews = calculateThisWeekViews();
    long uniqueVisitors = calculateTotalUniqueVisitors();

    List<DashboardSummaryResponse.PopularPost> topPosts = getTopPosts(5);
    Map<String, Long> topReferrers = getTopReferrers(5);
    Map<LocalDate, Long> weeklyTrend = getWeeklyTrend();

    return DashboardSummaryResponse.builder()
        .totalPosts(totalPosts)
        .totalViews(totalViews)
        .todayViews(todayViews)
        .thisWeekViews(thisWeekViews)
        .uniqueVisitors(uniqueVisitors)
        .topPosts(topPosts)
        .topReferrers(topReferrers)
        .weeklyTrend(weeklyTrend)
        .build();
  }

  @Cacheable(value = "post-stats", key = "#postId")
  public PostStatisticsResponse getPostStatistics(String postId) {

    Post post = mongoTemplate.findById(postId, Post.class);
    if (post == null) {
      throw new ResourceNotFoundException("post", postId);
    }

    PostStatistics stats =
        mongoTemplate.findOne(
            Query.query(Criteria.where("postId").is(postId)), PostStatistics.class);

    if (stats == null) {
      return PostStatisticsResponse.builder()
          .postId(postId)
          .title(post.getTitle())
          .slug(post.getSlug())
          .totalViews(post.getViewCount())
          .uniqueVisitors(0L)
          .returningVisitors(0L)
          .viewsByDay(new HashMap<>())
          .viewsByReferrer(new HashMap<>())
          .viewsByRegion(new HashMap<>())
          .build();
    }

    return PostStatisticsResponse.builder()
        .postId(postId)
        .title(post.getTitle())
        .slug(post.getSlug())
        .totalViews(stats.getTotalViews())
        .uniqueVisitors(stats.getUniqueVisitors())
        .returningVisitors(stats.getReturningVisitors())
        .viewsByDay(convertStringMapToLocalDateMap(stats.getViewsByDay()))
        .viewsByReferrer(stats.getViewsByReferrer())
        .viewsByRegion(stats.getViewsByRegion())
        .build();
  }

  private long calculateTotalViews() {
    AggregationResults<Document> results =
        mongoTemplate.aggregate(
            Aggregation.newAggregation(Aggregation.group().sum("totalViews").as("total")),
            PostStatistics.class,
            Document.class);

    if (results.getMappedResults().isEmpty()) {
      return 0L;
    }

    Object total = results.getMappedResults().getFirst().get("total");
    return total != null ? ((Number) total).longValue() : 0L;
  }

  private long calculateTodayViews() {
    String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

    AggregationResults<Document> results =
        mongoTemplate.aggregate(
            Aggregation.newAggregation(Aggregation.group().sum("viewsByDay." + today).as("total")),
            PostStatistics.class,
            Document.class);

    if (results.getMappedResults().isEmpty()) {
      return 0L;
    }

    Object total = results.getMappedResults().getFirst().get("total");
    return total != null ? ((Number) total).longValue() : 0L;
  }

  private long calculateThisWeekViews() {
    LocalDate weekStart = LocalDate.now().minusDays(6);

    String[] fieldNames =
        IntStream.range(0, 7)
            .mapToObj(
                i -> "viewsByDay." + weekStart.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE))
            .toArray(String[]::new);

    AggregationResults<Document> results =
        mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.group()
                    .sum(
                        ArithmeticOperators.Add.valueOf(fieldNames[0])
                            .add(fieldNames[1])
                            .add(fieldNames[2])
                            .add(fieldNames[3])
                            .add(fieldNames[4])
                            .add(fieldNames[5])
                            .add(fieldNames[6]))
                    .as("total")),
            PostStatistics.class,
            Document.class);

    if (results.getMappedResults().isEmpty()) {
      return 0L;
    }

    Object total = results.getMappedResults().getFirst().get("total");
    return total != null ? ((Number) total).longValue() : 0L;
  }

  private long calculateTotalUniqueVisitors() {
    AggregationResults<Document> results =
        mongoTemplate.aggregate(
            Aggregation.newAggregation(Aggregation.group().sum("uniqueVisitors").as("total")),
            PostStatistics.class,
            Document.class);

    if (results.getMappedResults().isEmpty()) {
      return 0L;
    }

    Object total = results.getMappedResults().getFirst().get("total");
    return total != null ? ((Number) total).longValue() : 0L;
  }

  private List<DashboardSummaryResponse.PopularPost> getTopPosts(int limit) {
    List<Post> posts =
        mongoTemplate.find(
            Query.query(new Criteria())
                .with(Sort.by(Sort.Direction.DESC, "viewCount"))
                .limit(limit),
            Post.class);

    return posts.stream()
        .map(
            post ->
                DashboardSummaryResponse.PopularPost.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .slug(post.getSlug())
                    .viewCount(post.getViewCount())
                    .build())
        .collect(Collectors.toList());
  }

  private Map<String, Long> getTopReferrers(int limit) {
    AggregationResults<Document> results =
        mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(Criteria.where("viewsByReferrer").exists(true)),
                Aggregation.project()
                    .and(ObjectOperators.ObjectToArray.valueOfToArray("$viewsByReferrer"))
                    .as("referrerArray"),
                Aggregation.unwind("referrerArray"),
                Aggregation.group("referrerArray.k").sum("referrerArray.v").as("totalViews"),
                Aggregation.sort(Sort.Direction.DESC, "totalViews"),
                Aggregation.limit(limit)),
            PostStatistics.class,
            Document.class);

    return results.getMappedResults().stream()
        .collect(
            Collectors.toMap(
                doc -> doc.getString("_id"),
                doc -> doc.getInteger("totalViews").longValue(),
                (existing, replacement) -> existing,
                LinkedHashMap::new));
  }

  private Map<LocalDate, Long> getWeeklyTrend() {
    LocalDate weekStart = LocalDate.now().minusDays(6);
    Map<LocalDate, Long> weeklyTrend = new LinkedHashMap<>();

    String[] fieldNames =
        IntStream.range(0, 7)
            .mapToObj(
                i -> "viewsByDay." + weekStart.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE))
            .toArray(String[]::new);

    AggregationResults<Document> results =
        mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.group()
                    .sum(fieldNames[0])
                    .as("day0")
                    .sum(fieldNames[1])
                    .as("day1")
                    .sum(fieldNames[2])
                    .as("day2")
                    .sum(fieldNames[3])
                    .as("day3")
                    .sum(fieldNames[4])
                    .as("day4")
                    .sum(fieldNames[5])
                    .as("day5")
                    .sum(fieldNames[6])
                    .as("day6")),
            PostStatistics.class,
            Document.class);

    if (!results.getMappedResults().isEmpty()) {
      Document result = results.getMappedResults().getFirst();

      for (int i = 0; i < 7; i++) {
        LocalDate date = weekStart.plusDays(i);
        Long views =
            Optional.ofNullable(result.get("day" + i))
                .map(value -> ((Number) value).longValue())
                .orElse(0L);
        weeklyTrend.put(date, views);
      }
    } else {
      for (int i = 0; i < 7; i++) {
        weeklyTrend.put(weekStart.plusDays(i), 0L);
      }
    }

    return weeklyTrend;
  }

  private Map<LocalDate, Long> convertStringMapToLocalDateMap(Map<String, Long> stringMap) {
    if (stringMap == null) {
      return new HashMap<>();
    }

    return stringMap.entrySet().stream()
        .collect(Collectors.toMap(entry -> LocalDate.parse(entry.getKey()), Map.Entry::getValue));
  }
}
