package com.lostandfound.app.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostandfound.app.dto.response.BaseErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        log.warn("[{}] Access denied at {}: {}", getTraceId(), request.getRequestURI(), accessDeniedException.getMessage());

        BaseErrorResponse errorBody = BaseErrorResponse.builderWithTrace()
                .httpStatus(errorCode.getHttpStatus().value())
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(request.getRequestURI())
                .build();

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), errorBody);
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "N/A");
    }
}