package com.piper_trail.blog.shared.event;

import lombok.Getter;

import java.util.List;

// 검색 인덱스 수정, 캐시 무효화에서 사용
@Getter
public class PostUpdatedEvent extends AbstractDomainEvent {

  private final String postId;
  private final String title;
  private final String slug;
  private final String category;
  private final List<String> tags;

  private final String previousTitle;
  private final String previousSlug;

  public PostUpdatedEvent(
      String postId,
      String title,
      String slug,
      String category,
      List<String> tags,
      String previousTitle,
      String previousSlug) {
    super();
    this.postId = postId;
    this.title = title;
    this.slug = slug;
    this.category = category;
    this.tags = tags;
    this.previousTitle = previousTitle;
    this.previousSlug = previousSlug;
  }

  @Override
  public String getAggregateId() {
    return postId;
  }

  public boolean isTitleChanged() {
    return !title.equals(previousTitle);
  }

  public boolean isSlugChanged() {
    return !slug.equals(previousSlug);
  }
}
