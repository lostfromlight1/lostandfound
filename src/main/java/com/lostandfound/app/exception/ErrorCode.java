package com.lostandfound.app.exception;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** Enumeration of application-specific error codes and their associated HTTP status. */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // --- Authentication & User ---
  AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "Authentication failed"),
  AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "Authentication is required to access this resource"),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_003", "You do not have permission to perform this action"),
  USER_LOCKED(HttpStatus.FORBIDDEN, "USR_001", "This account has been locked by an administrator"),
  EMAIL_ALREADY_IN_USE(HttpStatus.CONFLICT, "USR_002", "This email address is already registered"),
  USERNAME_ALREADY_IN_USE(HttpStatus.CONFLICT, "USR_003", "This username is already taken"),

  // --- Validation & Request ---
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "REQ_001", "One or more fields failed validation"),
  INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "REQ_002", "Malformed request body"),

  // --- Media & Uploads ---
  FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "MED_001", "The uploaded file exceeds the maximum allowed size"),
  UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "MED_002", "Only image files (JPEG, PNG) are allowed"),
  IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MED_003", "Maximum number of images per post exceeded"),
  IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MED_004", "Failed to save the uploaded image"),

  // --- Resource Management ---
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_001", "Resource not found"),
  RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "RES_002", "Resource already exists"),

  // --- Post & Business Logic ---
  BUSINESS_ERROR(HttpStatus.BAD_REQUEST, "BIZ_001", "Business rule violation"),
  POST_MODIFICATION_DENIED(HttpStatus.FORBIDDEN, "PST_001", "You can only modify or delete your own posts"),
  INVALID_POST_STATUS(HttpStatus.BAD_REQUEST, "PST_002", "Invalid status transition for this post"),
  POST_ALREADY_RESOLVED(HttpStatus.BAD_REQUEST, "PST_003", "This item has already been resolved and cannot be modified"),

  // --- Security & Tokens ---
  TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "SEC_001", "Security token is invalid"),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "SEC_002", "Security token has expired"),

  // --- System Errors ---
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "Internal server error");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  /** Helper to find ErrorCode by string code if needed for external mapping. */
  public static ErrorCode fromCode(String code) {
    return Arrays.stream(values())
            .filter(error -> error.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(INTERNAL_ERROR);
  }
}