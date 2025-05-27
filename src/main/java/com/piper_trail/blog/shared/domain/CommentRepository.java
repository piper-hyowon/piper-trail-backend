package com.piper_trail.blog.shared.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

  @Query("{'postId': ?0, 'approved': true, 'hidden': false}")
  Page<Comment> findByPostId(String postId, Pageable pageable);

  @Query(value = "{'ipAddress': ?0, 'createdAt': {$gte: ?1}}", count = true)
  long countRecentByIpAddress(String ipAddress, Instant after);
}
