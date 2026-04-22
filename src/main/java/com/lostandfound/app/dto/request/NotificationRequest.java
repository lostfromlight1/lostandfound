package com.lostandfound.app.dto.request;

import com.lostandfound.app.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificationRequest {

    /**
     * Request to mark notification as read
     */
    public record MarkAsReadRequest(
            @NotNull(message = "Notification ID is required")
            Long notificationId
    ) {}

    /**
     * Request to mark all notifications as read
     */
    public record MarkAllAsReadRequest(

    ) {}

    /**
     * Request to delete a notification
     */
    public record DeleteNotificationRequest(
            @NotNull(message = "Notification ID is required")
            Long notificationId
    ) {}

    /**
     * Request to save FCM token
     */
    public record SaveFcmTokenRequest(
            @NotBlank(message = "FCM token is required")
            @Size(max = 500, message = "FCM token is too long")
            String fcmToken
    ) {}

    /**
     * Request to create manual notification (admin only)
     */
    public record CreateNotificationRequest(
            @NotNull(message = "Recipient ID is required")
            Long recipientId,

            @NotNull(message = "Notification type is required")
            NotificationType type,

            @NotBlank(message = "Title is required")
            @Size(max = 255, message = "Title must be less than 255 characters")
            String title,

            @NotBlank(message = "Message is required")
            @Size(max = 2000, message = "Message must be less than 2000 characters")
            String message,

            Long postId,

            Long commentId,

            Long replyId
    ) {}

    /**
     * Internal request for creating notifications (from other services)
     */
    public record InternalNotificationRequest(
            Long recipientId,
            Long userId,
            NotificationType type,
            String title,
            String message,
            Long postId,
            Long commentId,
            Long replyId,
            String fcmToken
    ) {}
}
