package com.piper_trail.blog.command.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentReviewRequest {
  @NotNull private Boolean approved;
  private String reason;
}
