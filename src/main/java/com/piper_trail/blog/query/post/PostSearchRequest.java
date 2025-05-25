package com.piper_trail.blog.query.post;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
@Builder
public class PostSearchRequest {
  private String keyword;
  private String category;
  private List<String> tags;
  private int page;
  private int size;
  private String sortBy;
  private Sort.Direction sortDirection;
}
