package com.lostandfound.app.dto.response;

import lombok.Builder;
import lombok.Data;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
public class BaseErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private int httpStatus;
    private String errorCode;
    private String message;
    private String path;
    private String apiId;
    private String traceId;
    private Map<String, String> validationErrors;

    /**
     * Custom builder method to automatically inject the traceId from the logging context.
     * If no traceId exists, it generates a fallback UUID.
     */
    public static BaseErrorResponseBuilder builderWithTrace() {
        String traceId = MDC.get("traceId");
        return builder().traceId(Objects.toString(traceId, "FALLBACK-" + UUID.randomUUID().toString().substring(0, 8)));
    }
}