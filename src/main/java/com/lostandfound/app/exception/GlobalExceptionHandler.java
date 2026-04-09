package com.lostandfound.app.exception;

import com.lostandfound.app.dto.response.BaseErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ------------------ Application-specific errors ------------------

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseErrorResponse> handleAppException(
            AppException ex, HttpServletRequest request) {
        return buildAndLogResponse(ex.getErrorCode(), request, ex.getMessage(), null);
    }

    // ------------------ Validation errors ------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .collect(
                                Collectors.toMap(
                                        FieldError::getField,
                                        e -> Objects.toString(e.getDefaultMessage(), "Invalid value"),
                                        (existing, replacement) -> existing));
        return buildAndLogResponse(
                ErrorCode.VALIDATION_ERROR, request, "Request validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors =
                ex.getConstraintViolations().stream()
                        .collect(
                                Collectors.toMap(
                                        v -> v.getPropertyPath().toString(),
                                        v -> Objects.toString(v.getMessage(), "Invalid value"),
                                        (existing, replacement) -> existing));
        return buildAndLogResponse(
                ErrorCode.VALIDATION_ERROR, request, "Parameter validation failed", errors);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildAndLogResponse(
                ErrorCode.FILE_TOO_LARGE,
                request,
                "The uploaded file exceeds the maximum allowed size of 20MB.",
                null);
    }

    // ------------------ Security errors ------------------

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseErrorResponse> handleAuth(BadCredentialsException ex, HttpServletRequest request) {
        return buildAndLogResponse(ErrorCode.AUTH_FAILED, request, "Invalid email or password", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildAndLogResponse(ErrorCode.ACCESS_DENIED, request, "Access denied", null);
    }

    // ------------------ Malformed request ------------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseErrorResponse> handleJsonError(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildAndLogResponse(
                ErrorCode.INVALID_REQUEST_BODY, request, "Malformed JSON request body", null);
    }

    // ------------------ Spring Web Routing Errors ------------------

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseErrorResponse> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return buildAndLogResponse(
                ErrorCode.VALIDATION_ERROR,
                request,
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.",
                null);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<BaseErrorResponse> handleMissingParams(
            org.springframework.web.bind.MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return buildAndLogResponse(
                ErrorCode.VALIDATION_ERROR,
                request,
                "Missing required parameter: '" + ex.getParameterName() + "'",
                null);
    }

    // ------------------ Catch-all system errors ------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseErrorResponse> handleGlobal(Exception ex, HttpServletRequest request) {
        String traceId = getTraceId();
        log.error(
                "[{}][INTERNAL_ERROR] Unhandled exception at path {}: {}",
                traceId,
                request.getRequestURI(),
                ex.getMessage(),
                ex);
        return buildResponse(
                ErrorCode.INTERNAL_ERROR,
                request.getRequestURI(),
                "An unexpected server error occurred. TraceId: " + traceId,
                null,
                request);
    }

    // ------------------ Internal helpers ------------------

    private ResponseEntity<BaseErrorResponse> buildAndLogResponse(
            ErrorCode code,
            HttpServletRequest request,
            String customMessage,
            Map<String, String> validationErrors) {

        String traceId = getTraceId();
        String message = customMessage != null ? customMessage : code.getMessage();

        log.warn(
                "[{}][{}] Path: {}, Message: {}, ValidationErrors: {}",
                traceId,
                code.getCode(),
                request.getRequestURI(),
                message,
                validationErrors);

        return buildResponse(code, request.getRequestURI(), message, validationErrors, request);
    }

    private ResponseEntity<BaseErrorResponse> buildResponse(
            ErrorCode code, String path, String message, Map<String, String> validationErrors, HttpServletRequest request) {

        String apiId = (String) request.getAttribute("apiId");

        BaseErrorResponse response =
                BaseErrorResponse.builderWithTrace()
                        .httpStatus(code.getHttpStatus().value())
                        .errorCode(code.getCode())
                        .message(message)
                        .path(path)
                        .apiId(apiId)
                        .validationErrors(validationErrors != null ? validationErrors : Map.of())
                        .build();

        return new ResponseEntity<>(response, code.getHttpStatus());
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}