package com.piper_trail.blog.command.comment;

import com.piper_trail.blog.shared.domain.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

  @NotBlank
  @Size(max = 20)
  private String author;

  @NotBlank
  @Size(min = 4, max = 20)
  private String password;

  @NotBlank
  @Size(max = 1000)
  private String content;

  private Comment.FontFamily fontFamily = Comment.FontFamily.DEFAULT;
  private Comment.TextColor textColor = Comment.TextColor.DEFAULT;
}
