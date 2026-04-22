package com.lostandfound.app.service;

import com.lostandfound.app.dto.response.NotificationResponse;

/**
 * FCM Service Interface
 * Handles Firebase Cloud Messaging for push notifications
 */
public interface FcmService {

    /**
     * Send push notification to a single device
     *
     * @param fcmToken Device FCM token
     * @param title Notification title
     * @param body Notification body/message
     * @param data Additional data payload
     * @return True if sent successfully
     */
    boolean sendPushNotification(String fcmToken, String title, String body, java.util.Map<String, String> data);

    /**
     * Send push notification using notification object
     *
     * @param notification Notification object to send
     * @return True if sent successfully
     */
    boolean sendNotificationAsPush(NotificationResponse.NotificationDto notification);

    /**
     * Send notification to multiple devices
     *
     * @param fcmTokens List of device FCM tokens
     * @param title Notification title
     * @param body Notification body
     * @return Number of successfully sent notifications
     */
    int sendMulticastNotification(java.util.List<String> fcmTokens, String title, String body);

    /**
     * Check if FCM is configured and available
     *
     * @return True if FCM is available
     */
    boolean isFcmAvailable();
}
