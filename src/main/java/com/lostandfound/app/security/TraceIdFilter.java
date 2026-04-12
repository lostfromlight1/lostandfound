package com.lostandfound.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

  private static final String TRACE_KEY = "traceId";
  private static final String TRACE_HEADER = "X-Trace-Id";
  private static final int TRACE_LENGTH = 12;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String traceId = request.getHeader(TRACE_HEADER);

    if (traceId == null || traceId.isBlank()) {
      traceId = generateTraceId();
    }

    MDC.put(TRACE_KEY, traceId);
    response.setHeader(TRACE_HEADER, traceId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(TRACE_KEY);
    }
  }

  private String generateTraceId() {
    // Generate 12-character trace ID from UUID
    return UUID.randomUUID().toString().replace("-", "").substring(0, TRACE_LENGTH);
  }
}
