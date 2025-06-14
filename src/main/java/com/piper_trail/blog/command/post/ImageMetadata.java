package com.piper_trail.blog.command.post;

import lombok.Data;

@Data
public class ImageMetadata {
  private String id;
  private String placeholder;
  private String filename;
  private int index;
}
