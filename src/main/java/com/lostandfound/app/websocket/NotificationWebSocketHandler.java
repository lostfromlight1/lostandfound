package com.lostandfound.app.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostandfound.app.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void sendNotificationToUser(Long userId, NotificationResponse.WebSocketNotification notification) {
        try {
            String destination = "/queue/notifications/" + userId;
            messagingTemplate.convertAndSend(destination, notification);
            log.info("WebSocket notification sent to user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error sending WebSocket notification to user ID: {}", userId, e);
        }
    }

    public void broadcastNotification(NotificationResponse.WebSocketNotification notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("Broadcast notification sent to all users");
        } catch (Exception e) {
            log.error("Error broadcasting notification", e);
        }
    }

    public void sendUnreadCountUpdate(Long userId, long unreadCount) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "UNREAD_COUNT_UPDATE");
            update.put("unreadCount", unreadCount);
            update.put("timestamp", System.currentTimeMillis());

            String destination = "/queue/notifications/" + userId + "/count";
            messagingTemplate.convertAndSend(destination, update);
            log.info("Unread count update sent to user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error sending unread count update to user ID: {}", userId, e);
        }
    }

    public void sendTypingIndicator(Long userId, boolean isTyping) {
        try {
            Map<String, Object> indicator = new HashMap<>();
            indicator.put("type", "TYPING_INDICATOR");
            indicator.put("userId", userId);
            indicator.put("isTyping", isTyping);

            messagingTemplate.convertAndSend("/topic/notifications/typing", indicator);
            log.debug("Typing indicator sent for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error sending typing indicator", e);
        }
    }
}
