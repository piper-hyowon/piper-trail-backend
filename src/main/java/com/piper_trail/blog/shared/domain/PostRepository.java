package com.piper_trail.blog.shared.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

  Optional<Post> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, String id);

  // List<Post> findByCategoryCreatedAtDesc(String category);

  // List<Post> findByTagsCreatedAtDesc(String tag);
}
