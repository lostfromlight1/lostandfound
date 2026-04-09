package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
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

        sendEmail(message, to, "Password reset");
    }

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        log.info("Sending email verification to: {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify Your Email - Lost & Found");
        message.setText("Hello,\n\n" +
                "Welcome to Lost & Found! Please verify your email address by clicking the link below:\n\n" +
                verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Thanks,\nThe Lost & Found Team");

        sendEmail(message, to, "Verification");
    }

    // Helper method to keep code DRY
    private void sendEmail(SimpleMailMessage message, String to, String type) {
        try {
            mailSender.send(message);
            log.info("{} email sent successfully to: {}", type, to);
        } catch (Exception e) {
            log.error("Failed to send {} email to {}: {}", type, to, e.getMessage());
        }
    }
}