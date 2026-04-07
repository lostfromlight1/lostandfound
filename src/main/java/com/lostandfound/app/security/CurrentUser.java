package com.lostandfound.app.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

// Custom meta-annotation to inject the authenticated User entity into Controller methods
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal // Tells Spring to resolve this parameter using the Security Context
public @interface CurrentUser {}