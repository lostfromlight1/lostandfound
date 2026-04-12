package com.lostandfound.app.service;

import com.lostandfound.app.repository.UserRepository;

import java.util.List;
import java.util.Objects;

import com.lostandfound.app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

        return userRepository.findByEmail(email)
                .map(user -> {
                    log.debug("[{}] User found: ID={}", tid, user.getId());

                    return new CustomUserDetails(
                            user.getId(),
                            user.getEmail(),
                            user.getPassword(),
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                })
                .orElseThrow(() -> {
                    log.warn("[{}] Auth failed: User not found with email: {}", tid, email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}

