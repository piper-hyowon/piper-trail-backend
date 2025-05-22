package com.piper_trail.blog.shared.exception;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String resourceType, String resourceId) {
    super(String.format("%s, id %s not found", resourceType, resourceId));
  }
}
