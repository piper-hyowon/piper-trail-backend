package com.piper_trail.blog.command.postcard;

import com.piper_trail.blog.shared.domain.StampType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostcardRequest {
  @NotNull private StampType stampType;

  @Size(max = 20)
  private String nickname;

  @Size(max = 200)
  private String message;
}
