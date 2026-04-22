package com.lostandfound.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration
 * Configures Firebase Admin SDK for push notifications
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-file:}")
    private String credentialsFile;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (!firebaseEnabled) {
            log.warn("Firebase is disabled. FCM notifications will not be sent.");
            return null;
        }

        try {
            InputStream serviceAccount = getServiceAccountInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }

            return FirebaseMessaging.getInstance();

        } catch (IOException e) {
            log.error("Failed to initialize Firebase. Make sure firebase-service-account-key.json exists.", e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    /**
     * Get service account input stream
     * Tries multiple paths to locate the credentials file
     */
    private InputStream getServiceAccountInputStream() throws IOException {
        // Try classpath first
        try {
            InputStream classpath = FirebaseConfig.class.getResourceAsStream("/firebase-service-account-key.json");
            if (classpath != null) {
                log.info("Using Firebase credentials from classpath");
                return classpath;
            }
        } catch (Exception e) {
            log.debug("Firebase credentials not found in classpath");
        }

        // Try file system
        if (credentialsFile != null && !credentialsFile.isEmpty()) {
            try {
                log.info("Using Firebase credentials from file: {}", credentialsFile);
                return new FileInputStream(credentialsFile);
            } catch (IOException e) {
                log.debug("Firebase credentials file not found at: {}", credentialsFile);
            }
        }

        throw new IOException("Firebase service account credentials file not found");
    }
}
