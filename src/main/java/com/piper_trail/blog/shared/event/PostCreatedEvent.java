package com.piper_trail.blog.shared.event;

import lombok.Getter;

import java.util.List;

// {검색 인덱스 업데이트, 통계 초기화}에서 사용
@Getter
public class PostCreatedEvent extends AbstractDomainEvent {

  private final String postId;
  private final String title;
  private final String slug;
  private final String category;
  private final List<String> tags;

  public PostCreatedEvent(
      String postId, String title, String slug, String category, List<String> tags) {
    super();
    this.postId = postId;
    this.title = title;
    this.slug = slug;
    this.category = category;
    this.tags = tags;
  }

  @Override
  public String getAggregateId() {
    return postId;
  }
}
