package com.lostandfound.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostandfound.app.config.SecurityProperties;
import com.lostandfound.app.dto.response.BaseErrorResponse;
import com.lostandfound.app.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestApiKey = request.getHeader(securityProperties.apiKey().header());
        String configuredApiKey = securityProperties.apiKey().value();

        if (requestApiKey == null || !requestApiKey.equals(configuredApiKey)) {
            log.warn("[{}] Blocked request to {} - Missing or Invalid API Key", getTraceId(), path);

            ErrorCode errorCode = ErrorCode.AUTH_REQUIRED;

            BaseErrorResponse errorBody = BaseErrorResponse.builderWithTrace()
                    .httpStatus(errorCode.getHttpStatus().value())
                    .errorCode(errorCode.getCode())
                    .message("Missing or Invalid Master API Key")
                    .path(path)
                    .build();

            response.setStatus(errorCode.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getOutputStream(), errorBody);

            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "N/A");
    }
}