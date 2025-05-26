package com.piper_trail.blog.shared.exception;

public class VisitorInvalidPasswordException extends RuntimeException {
  public enum VisitorAction {
    Comment,
    Chat,
    Stamp
  }

  public VisitorInvalidPasswordException(VisitorAction action) {
    super(action + ": Invalid visitor password");
  }
}
