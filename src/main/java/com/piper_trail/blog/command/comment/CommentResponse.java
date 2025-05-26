package com.piper_trail.blog.command.comment;

import com.piper_trail.blog.shared.domain.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CommentResponse {
  private String id;
  private String author;
  private String content;
  private Comment.FontFamily fontFamily;
  private Comment.TextColor textColor;
  private Instant createdAt;
  private boolean reviewNeeded;
}
