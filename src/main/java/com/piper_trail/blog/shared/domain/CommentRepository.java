package com.piper_trail.blog.shared.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

  @Query("{'postId': ?0, 'approved': true, 'hidden': false}")
  List<Comment> findVisibleByPostId(String postId);

  @Query("{'ipAddress': ?0, 'createdAt': {$gte: ?1}}")
  long countRecentByIpAddress(String ipAddress, Instant after);
}
