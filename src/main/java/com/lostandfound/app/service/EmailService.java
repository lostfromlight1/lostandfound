package com.lostandfound.app.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetLink);

    void sendVerificationEmail(String to, String verificationLink);
}