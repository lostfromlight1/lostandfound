package com.lostandfound.app.service;

import com.lostandfound.app.dto.response.NotificationResponse;

public interface FcmService {

    boolean sendPushNotification(String fcmToken, String title, String body, java.util.Map<String, String> data);

    boolean sendNotificationAsPush(NotificationResponse.NotificationDto notification);

    int sendMulticastNotification(java.util.List<String> fcmTokens, String title, String body);

    boolean isFcmAvailable();
}
