package com.piper_trail.blog.command.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeleteCommentRequest {
  @NotBlank
  @Size(min = 4, max = 20)
  private String password;
}
