package com.piper_trail.blog.shared.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeriesRepository extends MongoRepository<Series, String> {
  Optional<Series> findBySlug(String slug);
  Optional<Series> findByTitle(String title);
  boolean existsBySlug(String slug);
}
