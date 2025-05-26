package com.piper_trail.blog.command.comment;

import lombok.Data;

@Data
public class CommentStatusUpdateRequest {
  private Boolean approved;
  private Boolean hidden;
  private Boolean needsReview;
  private String reason;
}
