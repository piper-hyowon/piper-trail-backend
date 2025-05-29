package com.piper_trail.blog.shared.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostcardRepository extends MongoRepository<Postcard, String> {
  Page<Postcard> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
