package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.Notification;
import com.lostandfound.app.model.NotificationStatus;
import com.lostandfound.app.model.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

public class NotificationResponse {

    @Builder
    public record NotificationDto(
            Long id,
            Long userId,
            String userName,
            String userAvatarUrl,
            Long recipientId,
            NotificationType type,
            NotificationStatus status,
            String title,
            String fcmToken,
            String message,
            Long postId,
            Long commentId,
            Long replyId,
            Boolean pushSent,
            LocalDateTime readAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static NotificationDto fromEntity(Notification notification) {
            return NotificationDto.builder()
                    .id(notification.getId())
                    .userId(notification.getUser().getId())
                    .userName(notification.getUser().getDisplayName())
                    .userAvatarUrl(notification.getUser().getAvatarUrl())
                    .recipientId(notification.getRecipient().getId())
                    .type(notification.getType())
                    .status(notification.getStatus())
                    .title(notification.getTitle())
                    .fcmToken(notification.getFcmToken())
                    .message(notification.getMessage())
                    .postId(notification.getPostId())
                    .commentId(notification.getCommentId())
                    .replyId(notification.getReplyId())
                    .pushSent(notification.getPushSent())
                    .readAt(notification.getReadAt())
                    .createdAt(notification.getCreatedAt())
                    .updatedAt(notification.getUpdatedAt())
                    .build();
        }
    }

    @Builder
    public record NotificationSummary(
            Long id,
            String userName,
            String userAvatarUrl,
            NotificationType type,
            NotificationStatus status,
            String title,
            LocalDateTime createdAt
    ) {
        public static NotificationSummary fromEntity(Notification notification) {
            return NotificationSummary.builder()
                    .id(notification.getId())
                    .userName(notification.getUser().getDisplayName())
                    .userAvatarUrl(notification.getUser().getAvatarUrl())
                    .type(notification.getType())
                    .status(notification.getStatus())
                    .title(notification.getTitle())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    /**
     * Unread notification count
     */
    @Builder
    public record UnreadCountDto(
            Long unreadCount
    ) {}

    /**
     * FCM token save response
     */
    @Builder
    public record FcmTokenResponse(
            Boolean saved,
            String message
    ) {}

    /**
     * Notification action response
     */
    @Builder
    public record NotificationActionResponse(
            Boolean success,
            String message,
            Long notificationId
    ) {}

    /**
     * WebSocket notification message (for STOMP)
     */
    @Builder
    public record WebSocketNotification(
            Long id,
            Long userId,
            String userName,
            NotificationType type,
            String title,
            String message,
            NotificationStatus status,
            LocalDateTime createdAt
    ) {
        public static WebSocketNotification fromEntity(Notification notification) {
            return WebSocketNotification.builder()
                    .id(notification.getId())
                    .userId(notification.getUser().getId())
                    .userName(notification.getUser().getDisplayName())
                    .type(notification.getType())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .status(notification.getStatus())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }
}
