package com.piper_trail.blog.command.series;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSeriesRequest {
  @NotBlank
  @Size(max = 200)
  private String title;

  @Size(max = 200)
  private String titleEn;

  @NotBlank
  @Size(max = 500)
  private String description;

  @Size(max = 500)
  private String descriptionEn;

  private List<String> tags;
}
