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

  private String seriesTitle; // null이면 일반 글
  private String seriesTitleEn;
  private Integer seriesOrder; // null이면 자동 할당
  private String seriesDescription;
  private String seriesDescriptionEn;
}
