package com.piper_trail.blog.shared.exception;

import lombok.Getter;

// TODO: code 추가
@Getter
public class ErrorResponse {
  private final String message;

  public ErrorResponse(String message) {
    this.message = message;
  }
}
