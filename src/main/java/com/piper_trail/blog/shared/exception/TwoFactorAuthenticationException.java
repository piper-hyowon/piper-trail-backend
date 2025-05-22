package com.piper_trail.blog.shared.exception;

public class TwoFactorAuthenticationException extends AuthenticationException {

  public TwoFactorAuthenticationException(String message) {
    super(message);
  }
}
