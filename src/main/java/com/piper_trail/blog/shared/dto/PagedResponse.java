package com.piper_trail.blog.shared.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedResponse<T> {

  private List<T> content;
  private int page;
  private int size;
  private long total;
}
