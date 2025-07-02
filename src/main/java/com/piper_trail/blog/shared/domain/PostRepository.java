package com.piper_trail.blog.shared.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

  Optional<Post> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, String id);

  // List<Post> findByCategoryCreatedAtDesc(String category);

  // List<Post> findByTagsCreatedAtDesc(String tag);

  @Query("{ 'series.seriesId': ?0 }")
  List<Post> findBySeriesId(String seriesId, Sort sort);

  @Query("{ 'series.seriesId': ?0, 'series.order': ?1 }")
  Optional<Post> findBySeriesIdAndOrder(String seriesId, int order);

  @Query(value = "{ 'series.seriesId': ?0, 'series.order': ?1 }", exists = true)
  boolean existsBySeriesIdAndOrder(String seriesId, int order);

  @Query(value = "{ 'series.seriesId': ?0 }", sort = "{ 'series.order': -1 }")
  List<Post> findLatestBySeriesId(String seriesId);

  @Query(value = "{ 'series.seriesId': ?0 }", count = true)
  long countBySeriesId(String seriesId);
}
