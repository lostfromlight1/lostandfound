package com.lostandfound.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String to, String resetLink) {
        log.info("Sending password reset email to: {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request - Lost & Found");
        message.setText("Hello,\n\n" +
                "You have requested to reset your password. Click the link below to change it:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 15 minutes. If you did not request this, please ignore this email.\n\n" +
                "Thanks,\nThe Lost & Found Team");

        try {
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Depending on your strictness, you could throw an AppException here
        }
    }
}