package com.lostandfound.app.service;

import com.lostandfound.app.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String tid = getTraceId();
        log.debug("[{}] Security lookup for email: {}", tid, email);

        return userRepository
                .findByEmail(email)
                .map(
                        user -> {
                            log.debug("[{}] User found: ID={}", tid, user.getId());
                            return user;
                        })
                .orElseThrow(
                        () -> {
                            log.warn("[{}] Auth failed: User not found with email: {}", tid, email);
                            return new UsernameNotFoundException("User not found with email: " + email);
                        });
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}

