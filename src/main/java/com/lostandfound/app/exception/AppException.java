package com.lostandfound.app.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Base exception for all managed application errors. Allows the service layer to throw specific
 * ErrorCodes that the GlobalExceptionHandler can process.
 */
@Getter
public class AppException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final ErrorCode errorCode;

  /** Constructs an exception using the default message from the ErrorCode. */
  public AppException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  /** Constructs an exception with a specific, detailed message to override the default. */
  public AppException(ErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
  }

  /**
   * Static utility to throw an exception directly. Usage: .orElseThrow(() ->
   * AppException.of(ErrorCode.RESOURCE_NOT_FOUND));
   */
  public static AppException of(ErrorCode errorCode) {
    return new AppException(errorCode);
  }
}
