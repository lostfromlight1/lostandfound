package com.lostandfound.app.config;

import com.lostandfound.app.annotation.ApiId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiIdInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            ApiId apiId = handlerMethod.getMethodAnnotation(ApiId.class);
            if (apiId != null) {
                request.setAttribute("apiId", apiId.value());
            }
        }
        return true;
    }
}