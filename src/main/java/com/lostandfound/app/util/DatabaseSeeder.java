package com.lostandfound.app.util;

import com.lostandfound.app.model.AuthProvider;
import com.lostandfound.app.model.Role;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database for default accounts...");

        // 1. Seed Admin Account
        if (!userRepository.existsByEmail("admin@lostandfound.com")) {
            log.info("Creating default Admin account...");

            User admin = User.builder()
                    .email("admin@lostandfound.com")
                    // Note: This password passes your strict Regex requirements
                    .password(passwordEncoder.encode("Admin@1234!"))
                    .displayName("System Admin")
                    .role(Role.ADMIN)
                    .provider(AuthProvider.LOCAL)
                    .isLocked(false)
                    .build();

            userRepository.save(admin);
            log.info("Admin account created (admin@lostandfound.com / Admin@1234!)");
        }

        // 2. Seed Normal User Account
        if (!userRepository.existsByEmail("user@lostandfound.com")) {
            log.info("Creating default User account...");

            User standardUser = User.builder()
                    .email("user@lostandfound.com")
                    .password(passwordEncoder.encode("User@1234!"))
                    .displayName("Test User")
                    .role(Role.USER)
                    .provider(AuthProvider.LOCAL)
                    .isLocked(false)
                    .build();

            userRepository.save(standardUser);
            log.info("Standard User account created (user@lostandfound.com / User@1234!)");
        }

        log.info("Database seeding check complete.");
    }
}