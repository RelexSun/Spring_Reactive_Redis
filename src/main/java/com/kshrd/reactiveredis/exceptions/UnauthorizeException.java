package com.kshrd.reactiveredis.exceptions;

public class UnauthorizeException extends RuntimeException {
  public UnauthorizeException(String message) {
    super(message);
  }
}
