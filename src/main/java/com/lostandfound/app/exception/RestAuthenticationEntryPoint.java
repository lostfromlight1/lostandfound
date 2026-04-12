package com.lostandfound.app.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostandfound.app.dto.response.BaseErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Determine specific error based on attributes set in the JwtFilter
        ErrorCode errorCode = ErrorCode.AUTH_REQUIRED;
        Object exceptionAttr = request.getAttribute("exception");

        if ("expired".equals(exceptionAttr)) {
            errorCode = ErrorCode.TOKEN_EXPIRED;
        } else if ("invalid".equals(exceptionAttr) || "invalid_token_type".equals(exceptionAttr)) {
            errorCode = ErrorCode.TOKEN_INVALID;
        }

        log.warn("[{}] Authentication failure at {}: {}", getTraceId(), request.getRequestURI(), errorCode.getMessage());
        writeErrorResponse(request, response, errorCode);
    }

    private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) throws IOException {
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