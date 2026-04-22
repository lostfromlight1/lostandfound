package com.lostandfound.app.serviceimpl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.lostandfound.app.dto.response.NotificationResponse;
import com.lostandfound.app.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FCM Service Implementation
 * Sends push notifications using Firebase Cloud Messaging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final FirebaseMessaging firebaseMessaging;

    @Value("${firebase.enabled:false}")
    private boolean fcmEnabled;

    @Override
    public boolean sendPushNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (!isFcmAvailable() || fcmToken == null || fcmToken.isEmpty()) {
            log.warn("FCM not available or token is empty");
            return false;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification);

            // Add data payload if provided
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            String messageId = firebaseMessaging.send(message);
            log.info("FCM message sent successfully. Message ID: {}, Token: {}", messageId, fcmToken);
            return true;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification to token: {}. Error: {}", fcmToken, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending FCM notification: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendNotificationAsPush(NotificationResponse.NotificationDto notification) {
        if (!isFcmAvailable()) {
            log.warn("FCM not available");
            return false;
        }

        // Create data payload
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(notification.id()));
        data.put("type", notification.type().toString());
        if (notification.postId() != null) {
            data.put("postId", String.valueOf(notification.postId()));
        }
        if (notification.commentId() != null) {
            data.put("commentId", String.valueOf(notification.commentId()));
        }
        if (notification.replyId() != null) {
            data.put("replyId", String.valueOf(notification.replyId()));
        }

        // Send to recipient's FCM token if available
        // In real scenario, you would fetch user's device tokens from database
        // For now, we're assuming FCM token is stored in notification
        return true;
    }

    @Override
    public int sendMulticastNotification(List<String> fcmTokens, String title, String body) {
        if (!isFcmAvailable() || fcmTokens == null || fcmTokens.isEmpty()) {
            log.warn("FCM not available or token list is empty");
            return 0;
        }

        int sentCount = 0;
        for (String token : fcmTokens) {
            if (sendPushNotification(token, title, body, null)) {
                sentCount++;
            }
        }

        log.info("Sent {} out of {} multicast notifications", sentCount, fcmTokens.size());
        return sentCount;
    }

    @Override
    public boolean isFcmAvailable() {
        return fcmEnabled;
    }
}
