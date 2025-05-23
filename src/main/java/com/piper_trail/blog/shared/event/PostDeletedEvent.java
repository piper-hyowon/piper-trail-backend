package com.piper_trail.blog.shared.event;

import lombok.Getter;

import java.util.List;

// 관련 데이터 정리, 캐시 무효화, 통계 업데이트에서 사용
@Getter
public class PostDeletedEvent extends AbstractDomainEvent {

  private final String postId;
  private final String title;
  private final String slug;
  private final String category;
  private final List<String> tags;
  private final long viewCount; // 삭제 시점 조회수

  public PostDeletedEvent(
      String postId,
      String title,
      String slug,
      String category,
      List<String> tags,
      long viewCount) {
    super();
    this.postId = postId;
    this.title = title;
    this.slug = slug;
    this.category = category;
    this.tags = tags;
    this.viewCount = viewCount;
  }

  @Override
  public String getAggregateId() {
    return postId;
  }
}
