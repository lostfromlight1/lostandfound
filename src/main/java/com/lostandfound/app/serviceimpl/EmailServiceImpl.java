package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.service.BaseService;
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
public class EmailServiceImpl extends BaseService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String to, String resetCode) {
        log.info("[{}] Preparing password reset email for: {}", getTraceId(), to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Code - Back2U");
        message.setText("Hello,\n\n" +
                "You requested to reset your password. Please enter the following 6-character code to reset it:\n\n" +
                "   [ " + resetCode + " ]   \n\n" +
                "This code will expire in 15 minutes. If you did not request this, please ignore this email and your password will remain unchanged.\n\n" +
                "Thanks,\nThe Back2U Team");

        sendEmail(message, to, "Password reset");
    }

    @Override
    public void sendVerificationEmail(String to, String verificationCode) {
        log.info("[{}] Preparing email verification code for: {}", getTraceId(), to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify Your Email - Back2U");
        message.setText("Hello,\n\n" +
                "Welcome to Back2U! Please verify your email address by entering the following 6-character code on the verification page:\n\n" +
                "   [ " + verificationCode + " ]   \n\n" +
                "This code will expire in 24 hours.\n\n" +
                "Thanks,\nThe Back2U Team");

        sendEmail(message, to, "Verification");
    }

    // Helper method to keep code DRY
    private void sendEmail(SimpleMailMessage message, String to, String type) {
        try {
            mailSender.send(message);
            log.info("[{}] {} email sent successfully to: {}", getTraceId(), type, to);
        } catch (Exception e) {
            log.error("[{}] Failed to send {} email to {}. Error: {}", getTraceId(), type, to, e.getMessage());
        }
    }
}