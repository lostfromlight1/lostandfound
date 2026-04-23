package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.NotificationRequest;
import com.lostandfound.app.dto.response.NotificationResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.model.NotificationType;
import com.lostandfound.app.security.CustomUserDetails;

import java.util.List;

public interface NotificationService {

    PageResponse<NotificationResponse.NotificationSummary> getNotifications(Long userId, int page, int size);

    PageResponse<NotificationResponse.NotificationSummary> getNotificationsByType(
            Long userId,
            NotificationType type,
            int page,
            int size
    );

    NotificationResponse.UnreadCountDto getUnreadCount(Long userId);

    List<NotificationResponse.NotificationDto> getUnreadNotifications(Long userId);

    NotificationResponse.NotificationActionResponse markAsRead(Long notificationId, CustomUserDetails userDetails);

    NotificationResponse.NotificationActionResponse markAllAsRead(CustomUserDetails userDetails);

    NotificationResponse.NotificationActionResponse deleteNotification(Long notificationId, CustomUserDetails userDetails);

    NotificationResponse.FcmTokenResponse saveFcmToken(Long userId, String fcmToken);

    void createNotification(NotificationRequest.InternalNotificationRequest request);

    void createNotificationAsync(NotificationRequest.InternalNotificationRequest request);

    void notifyCommentOnPost(Long postId, Long commenterId, String commenterName);

    void notifyReplyOnComment(Long commentId, Long replyerId, String replierName);


    void notifyReplyOnReply(Long replyId, Long replyerId, String replierName);

    void notifyPostLiked(Long postId, Long likerId, String likerName);


    // 🔴 Report notification methods

    void notifyReportSubmitted(Long reportId, String reporterName, String targetType, Long targetId);

    void notifyReportResolved(Long reportId, Long reporterId, String targetType);

    void notifyReportRejected(Long reportId, Long reporterId, String targetType);

    /**
     * Send pending push notifications
     * Run periodically to send FCM notifications
     */
    void sendPendingPushNotifications();

    int deleteOldNotifications(java.time.LocalDateTime beforeDate);
}
