package com.piper_trail.blog.shared.event;

import lombok.Getter;

@Getter
public class CommentCreatedEvent extends AbstractDomainEvent {

  private final String commentId;
  private final String postId;
  private final String author;
  private final String content;
  private final double riskScore;

  public CommentCreatedEvent(
      String commentId, String postId, String author, String content, double riskScore) {
    super();
    this.commentId = commentId;
    this.postId = postId;
    this.author = author;
    this.content = content;
    this.riskScore = riskScore;
  }

  @Override
  public String getAggregateId() {
    return postId;
  }
}
