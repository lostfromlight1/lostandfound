package com.lostandfound.app.service;

import org.slf4j.MDC;

import java.util.Objects;

/**
 * Base class for all services to provide common utilities like trace ID.
 */
public abstract class BaseService {

    /**
     * Returns the current MDC trace ID, or "SYSTEM" if none set.
     */
    protected String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}
