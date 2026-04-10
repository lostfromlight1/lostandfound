//package com.lostandfound.app.config;
//
//import com.lostandfound.app.model.Role;
//import com.lostandfound.app.model.User;
//import com.lostandfound.app.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) throws Exception {
//        initializeAdminUser();
//    }
//
//    private void initializeAdminUser() {
//        String adminEmail = "admin@lostandfound.com";
//
//        if (userRepository.findByEmail(adminEmail).isEmpty()) {
//            log.info("No admin user found. Creating default administrator...");
//
//            User admin = User.builder()
//                    .email(adminEmail)
//                    .password(passwordEncoder.encode("admin123")) // Change this in production!
//                    .role(Role.ADMIN)
//                    .displayName("System Admin")
//                    .contactInfo("admin-support@lostandfound.com")
//                    .isLocked(false)
//                    .build();
//
//            // Note: Since User extends BaseEntity, ensure your BaseEntity
//            // logic handles setting 'isActive' to true by default.
//            admin.setActive(true);
//
//            userRepository.save(admin);
//            log.info("Admin user successfully created with email: {}", adminEmail);
//        } else {
//            log.info("Admin user already exists. Skipping initialization.");
//        }
//    }
//}