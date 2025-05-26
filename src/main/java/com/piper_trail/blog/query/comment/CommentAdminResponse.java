package com.piper_trail.blog.query.comment;

import com.piper_trail.blog.shared.domain.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CommentAdminResponse {
  private String id;
  private String postId;
  private String author;
  private String content;
  private Comment.FontFamily fontFamily;
  private Comment.TextColor textColor;
  private String ipAddress;
  private boolean approved;
  private boolean hidden;
  private boolean needsReview;
  private double riskScore;
  private String reviewReason;
  private Instant reviewedAt;
  private Instant createdAt;
  private Instant updatedAt;
}
