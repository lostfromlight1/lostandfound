package com.lostandfound.app.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

/**
 * This is a "Meta-Annotation."
 * When you put this on a parameter in your Controller, Spring looks
 * at the Security Context (where we stored the user in the JwtFilter)
 * and gives it directly to your method.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal // Tells Spring to resolve this parameter using the Security Context
public @interface CurrentUser {}