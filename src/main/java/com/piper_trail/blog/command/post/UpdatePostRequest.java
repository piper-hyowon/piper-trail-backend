package com.piper_trail.blog.command.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdatePostRequest {
  @NotBlank
  @Size(max = 200)
  private String title;

  @NotBlank
  @Size(max = 200)
  private String subtitle;

  @NotBlank private String markdownContent;

  @Size(max = 200)
  private String titleEn;

  @Size(max = 200)
  private String subtitleEn;

  private String markdownContentEn;

  @Size(max = 50)
  private String category;

  private List<String> tags = new ArrayList<>();
}
