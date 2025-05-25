package com.piper_trail.blog.shared.event;

import lombok.Getter;

@Getter
public class PostViewedEvent extends AbstractDomainEvent {

  private final String postId;
  private final String visitorId;
  private final String ipAddress;
  private final String userAgent;
  private final String referer;

  public PostViewedEvent(
      String postId, String visitorId, String ipAddress, String userAgent, String referer) {
    super();
    this.postId = postId;
    this.visitorId = visitorId;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.referer = referer;
  }

  @Override
  public String getAggregateId() {
    return postId;
  }
}
