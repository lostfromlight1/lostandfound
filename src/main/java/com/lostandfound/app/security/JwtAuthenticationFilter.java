// src/main/java/com/lostandfound/app/security/JwtAuthenticationFilter.java

package com.lostandfound.app.security;

import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = extractTokenFromHeader(request);
        if (jwt == null) {
            jwt = extractTokenFromCookie(request);
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.extractAllClaims(jwt);
            String userEmail = claims.getSubject();

            if (!"access".equals(claims.get("tokenType"))) {
                log.warn("[{}] Access denied: Invalid token type used at {}", jwtService.getTraceId(), request.getRequestURI());
                request.setAttribute("exception", "invalid_token_type");
            }
            else if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 1. Load UserDetails for JWT validation
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 2. CRITICAL FIX: Fetch the actual User entity from the DB
                    User actualUserEntity = userRepository.findByEmail(userEmail).orElse(null);

                    if (actualUserEntity != null) {
                        // 3. Pass the actual entity as the Principal so @CurrentUser works!
                        setAuthentication(request, actualUserEntity, userDetails);
                        log.debug("[{}] User '{}' authenticated for {}", jwtService.getTraceId(), userDetails.getUsername(), request.getRequestURI());
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("[{}] JWT Token expired for path: {}", jwtService.getTraceId(), request.getRequestURI());
            request.setAttribute("exception", "expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[{}] JWT validation failed at {}: {}", jwtService.getTraceId(), request.getRequestURI(), e.getMessage());
            request.setAttribute("exception", "invalid");
        } catch (Exception e) {
            log.error("[{}] Unexpected security filter error at {}", jwtService.getTraceId(), request.getRequestURI(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return java.util.Arrays.stream(request.getCookies())
                .filter(cookie -> "app_access_token".equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void setAuthentication(HttpServletRequest request, User actualUserEntity, UserDetails userDetails) {
        var authToken = new UsernamePasswordAuthenticationToken(actualUserEntity, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}