package com.lostandfound.app.dto.response;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

  @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();
  private String apiId;
  private String traceId;
  private String message;
  private T data;

  // ---------------------- Helpers ----------------------

  /** Returns a successful response with HTTP 200 */
  public static <T> ResponseEntity<BaseResponse<T>> success(String message, T data) {
    return ResponseEntity.ok(buildResponse(message, data));
  }

  /** Returns a successful response with HTTP 200 and no data payload */
  public static ResponseEntity<BaseResponse<Void>> success(String message) {
    return ResponseEntity.ok(buildResponse(message, null));
  }

  /** Returns a created response with HTTP 201 */
  public static <T> ResponseEntity<BaseResponse<T>> created(String message, T data) {
    return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(message, data));
  }

  // ---------------------- Internal ----------------------

  /** Constructs the response and automatically fills apiId and traceId from MDC */
  private static <T> BaseResponse<T> buildResponse(String message, T data) {
    return BaseResponse.<T>builder()
        .apiId(Objects.toString(MDC.get("apiId"), "N/A"))
        .traceId(Objects.toString(MDC.get("traceId"), "SYSTEM"))
        .message(message)
        .data(data)
        .build();
  }
}
