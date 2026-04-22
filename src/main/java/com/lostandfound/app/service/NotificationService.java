package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.NotificationRequest;
import com.lostandfound.app.dto.response.NotificationResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.model.NotificationType;
import com.lostandfound.app.security.CustomUserDetails;

import java.util.List;

public interface NotificationService {

    /**
     * Get paginated notifications for a user
     *
     * @param userId User ID
     * @param page Page number
     * @param size Page size
     * @return Paginated notifications
     */
    PageResponse<NotificationResponse.NotificationSummary> getNotifications(Long userId, int page, int size);

    /**
     * Get notifications by type
     *
     * @param userId User ID
     * @param type Notification type
     * @param page Page number
     * @param size Page size
     * @return Paginated notifications of specific type
     */
    PageResponse<NotificationResponse.NotificationSummary> getNotificationsByType(
            Long userId,
            NotificationType type,
            int page,
            int size
    );

    /**
     * Get unread notification count for a user
     *
     * @param userId User ID
     * @return Count of unread notifications
     */
    NotificationResponse.UnreadCountDto getUnreadCount(Long userId);

    /**
     * Get all unread notifications for a user
     *
     * @param userId User ID
     * @return List of unread notifications
     */
    List<NotificationResponse.NotificationDto> getUnreadNotifications(Long userId);

    /**
     * Mark a notification as read
     *
     * @param notificationId Notification ID
     * @param userDetails Current user details
     * @return Success response
     */
    NotificationResponse.NotificationActionResponse markAsRead(Long notificationId, CustomUserDetails userDetails);

    /**
     * Mark all notifications as read for a user
     *
     * @param userDetails Current user details
     * @return Success response
     */
    NotificationResponse.NotificationActionResponse markAllAsRead(CustomUserDetails userDetails);

    /**
     * Delete a notification
     *
     * @param notificationId Notification ID
     * @param userDetails Current user details
     * @return Success response
     */
    NotificationResponse.NotificationActionResponse deleteNotification(Long notificationId, CustomUserDetails userDetails);

    /**
     * Save FCM token for push notifications
     *
     * @param userId User ID
     * @param fcmToken FCM device token
     * @return Success response
     */
    NotificationResponse.FcmTokenResponse saveFcmToken(Long userId, String fcmToken);

    /**
     * Create notification (internal use)
     *
     * @param request Internal notification request
     */
    void createNotification(NotificationRequest.InternalNotificationRequest request);

    /**
     * Create notification asynchronously
     * Used by other services (Comment, Reply, Like)
     *
     * @param request Notification request
     */
    void createNotificationAsync(NotificationRequest.InternalNotificationRequest request);

    /**
     * Notify post owner about comment
     * Called from CommentService
     *
     * @param postId Post ID
     * @param commenterId Commenter user ID
     * @param commenterName Commenter name
     */
    void notifyCommentOnPost(Long postId, Long commenterId, String commenterName);

    /**
     * Notify comment owner about reply
     * Called from ReplyService
     *
     * @param commentId Comment ID
     * @param replyerId Replier user ID
     * @param replierName Replier name
     */
    void notifyReplyOnComment(Long commentId, Long replyerId, String replierName);

    /**
     * Notify reply owner about nested reply
     * Called from ReplyService
     *
     * @param replyId Reply ID
     * @param replyerId Replier user ID
     * @param replierName Replier name
     */
    void notifyReplyOnReply(Long replyId, Long replyerId, String replierName);

    /**
     * Notify post owner about like
     * Called from PostLikeService
     *
     * @param postId Post ID
     * @param likerId Liker user ID
     * @param likerName Liker name
     */
    void notifyPostLiked(Long postId, Long likerId, String likerName);


    // 🔴 Report notification methods
    /**
     * Notify all admins about new report submission
     */
    void notifyReportSubmitted(Long reportId, String reporterName, String targetType, Long targetId);

    /**
     * Notify reporter that their report has been resolved
     */
    void notifyReportResolved(Long reportId, Long reporterId, String targetType);

    /**
     * Notify reporter that their report has been rejected
     */
    void notifyReportRejected(Long reportId, Long reporterId, String targetType);

    /**
     * Send pending push notifications
     * Run periodically to send FCM notifications
     */
    void sendPendingPushNotifications();
}
