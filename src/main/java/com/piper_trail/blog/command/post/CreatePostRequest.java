package com.piper_trail.blog.command.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreatePostRequest {
  @NotBlank
  @Size(max = 200)
  private String title;

  @NotBlank private String markdownContent;

  @Size(max = 50)
  private String category;

  private List<String> tags = new ArrayList<>();
}
