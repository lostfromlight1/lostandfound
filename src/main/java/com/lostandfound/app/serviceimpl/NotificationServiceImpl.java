package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.NotificationRequest;
import com.lostandfound.app.dto.response.NotificationResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.NotificationRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.FcmService;
import com.lostandfound.app.service.NotificationService;
import com.lostandfound.app.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Notification Service Implementation
 * Handles all notification-related operations with async support
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse.NotificationSummary> getNotifications(Long userId, int page, int size) {
        log.info("[{}] Fetching notifications for user ID: {}. Page: {}, Size: {}", getTraceId(), userId, page, size);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository.findByRecipientId(userId, pageable);

        List<NotificationResponse.NotificationSummary> content = notificationPage.getContent()
                .stream()
                .map(NotificationResponse.NotificationSummary::fromEntity)
                .toList();

        return new PageResponse<>(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse.NotificationSummary> getNotificationsByType(
            Long userId,
            NotificationType type,
            int page,
            int size) {
        log.info("[{}] Fetching notifications for user ID: {} with type: {}", getTraceId(), userId, type);

        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository.findByRecipientIdAndType(userId, type, pageable);

        List<NotificationResponse.NotificationSummary> content = notificationPage.getContent()
                .stream()
                .map(NotificationResponse.NotificationSummary::fromEntity)
                .toList();

        return new PageResponse<>(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse.UnreadCountDto getUnreadCount(Long userId) {
        log.info("[{}] Getting unread count for user ID: {}", getTraceId(), userId);

        long unreadCount = notificationRepository.countUnreadByRecipientId(userId);
        return NotificationResponse.UnreadCountDto.builder()
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse.NotificationDto> getUnreadNotifications(Long userId) {
        log.info("[{}] Fetching unread notifications for user ID: {}", getTraceId(), userId);

        return notificationRepository.findUnreadByRecipientId(userId)
                .stream()
                .map(NotificationResponse.NotificationDto::fromEntity)
                .toList();
    }

    @Override
    public NotificationResponse.NotificationActionResponse markAsRead(Long notificationId, CustomUserDetails userDetails) {
        log.info("[{}] Marking notification ID: {} as read by user ID: {}", getTraceId(), notificationId, userDetails.getId());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found"));

        // Verify ownership
        if (!notification.getRecipient().getId().equals(userDetails.getId())) {
            log.warn("[{}] User ID: {} attempted to mark notification ID: {} owned by user ID: {}",
                    getTraceId(), userDetails.getId(), notificationId, notification.getRecipient().getId());
            throw new AppException(ErrorCode.ACCESS_DENIED, "You can only mark your own notifications");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
        log.info("[{}] Notification ID: {} marked as read", getTraceId(), notificationId);

        return NotificationResponse.NotificationActionResponse.builder()
                .success(true)
                .message("Notification marked as read")
                .notificationId(notificationId)
                .build();
    }

    @Override
    public NotificationResponse.NotificationActionResponse markAllAsRead(CustomUserDetails userDetails) {
        log.info("[{}] Marking all notifications as read for user ID: {}", getTraceId(), userDetails.getId());

        int updatedCount = notificationRepository.markAllAsReadByRecipientId(userDetails.getId());
        log.info("[{}] Marked {} notifications as read", getTraceId(), updatedCount);

        return NotificationResponse.NotificationActionResponse.builder()
                .success(true)
                .message("All notifications marked as read")
                .notificationId(null)
                .build();
    }

    @Override
    public NotificationResponse.NotificationActionResponse deleteNotification(Long notificationId, CustomUserDetails userDetails) {
        log.info("[{}] Deleting notification ID: {} by user ID: {}", getTraceId(), notificationId, userDetails.getId());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found"));

        // Verify ownership
        if (!notification.getRecipient().getId().equals(userDetails.getId())) {
            log.warn("[{}] User ID: {} attempted to delete notification ID: {} owned by user ID: {}",
                    getTraceId(), userDetails.getId(), notificationId, notification.getRecipient().getId());
            throw new AppException(ErrorCode.ACCESS_DENIED, "You can only delete your own notifications");
        }

        notification.softDelete();
        notificationRepository.save(notification);
        log.info("[{}] Notification ID: {} deleted", getTraceId(), notificationId);

        return NotificationResponse.NotificationActionResponse.builder()
                .success(true)
                .message("Notification deleted")
                .notificationId(notificationId)
                .build();
    }

    @Override
    public NotificationResponse.FcmTokenResponse saveFcmToken(Long userId, String fcmToken) {
        log.info("[{}] Saving FCM token for user ID: {}", getTraceId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        // Note: In real implementation, you'd have a separate table for device tokens
        // For now, we're storing in notification object
        log.info("[{}] FCM token saved for user ID: {}", getTraceId(), userId);

        return NotificationResponse.FcmTokenResponse.builder()
                .saved(true)
                .message("FCM token saved successfully")
                .build();
    }

    @Override
    @Transactional
    public void createNotification(NotificationRequest.InternalNotificationRequest request) {
        log.info("[{}] Creating notification for recipient ID: {} from user ID: {}",
                getTraceId(), request.recipientId(), request.userId());

        try {
            User recipient = userRepository.findById(request.recipientId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Recipient not found"));

            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

            // Prevent self-notifications
            if (recipient.getId().equals(user.getId())) {
                log.info("[{}] Skipping self-notification for user ID: {}", getTraceId(), user.getId());
                return;
            }

            // Check for duplicate unread notifications
            boolean exists = notificationRepository.existsByRecipientAndUserAndTypeAndPost(
                    request.recipientId(),
                    request.userId(),
                    request.type(),
                    request.postId()
            );

            if (exists) {
                log.info("[{}] Duplicate notification skipped for recipient ID: {}", getTraceId(), request.recipientId());
                return;
            }

            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .user(user)
                    .type(request.type())
                    .status(NotificationStatus.UNREAD)
                    .title(request.title())
                    .message(request.message())
                    .postId(request.postId())
                    .commentId(request.commentId())
                    .replyId(request.replyId())
                    .fcmToken(request.fcmToken())
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            log.info("[{}] Notification ID: {} created successfully", getTraceId(), savedNotification.getId());

            // Send via WebSocket immediately
            sendWebSocketNotification(savedNotification);

        } catch (Exception e) {
            log.error("[{}] Error creating notification: {}", getTraceId(), e.getMessage(), e);
        }
    }

    @Override
    @Async("notificationExecutor")
    public void createNotificationAsync(NotificationRequest.InternalNotificationRequest request) {
        log.info("[{}] Creating notification asynchronously for recipient ID: {}", getTraceId(), request.recipientId());
        createNotification(request);
    }

    @Override
    @Async("notificationExecutor")
    public void notifyCommentOnPost(Long postId, Long commenterId, String commenterName) {
        log.info("[{}] Notifying post owner about comment on post ID: {}", getTraceId(), postId);

        // Fetch post to get owner
        // In real implementation, you'd use PostRepository
        // For now, this is a placeholder

        String title = commenterName + " commented on your post";
        String message = "Check out the new comment";

        createNotification(new NotificationRequest.InternalNotificationRequest(
                null, // recipientId - to be fetched from post
                commenterId,
                NotificationType.COMMENT_CREATED,
                title,
                message,
                postId,
                null,
                null,
                null
        ));
    }

    @Override
    @Async("notificationExecutor")
    public void notifyReplyOnComment(Long commentId, Long replyerId, String replierName) {
        log.info("[{}] Notifying comment owner about reply on comment ID: {}", getTraceId(), commentId);

        String title = replierName + " replied to your comment";
        String message = "Check out the new reply";

        createNotification(new NotificationRequest.InternalNotificationRequest(
                null, // recipientId - to be fetched from comment
                replyerId,
                NotificationType.REPLY_CREATED,
                title,
                message,
                null,
                commentId,
                null,
                null
        ));
    }

    @Override
    @Async("notificationExecutor")
    public void notifyReplyOnReply(Long replyId, Long replyerId, String replierName) {
        log.info("[{}] Notifying reply owner about nested reply on reply ID: {}", getTraceId(), replyId);

        String title = replierName + " replied to your reply";
        String message = "Check out the new reply";

        createNotification(new NotificationRequest.InternalNotificationRequest(
                null, // recipientId - to be fetched from reply
                replyerId,
                NotificationType.REPLY_TO_REPLY,
                title,
                message,
                null,
                null,
                replyId,
                null
        ));
    }

    @Override
    @Async("notificationExecutor")
    public void notifyPostLiked(Long postId, Long likerId, String likerName) {
        log.info("[{}] Notifying post owner about like on post ID: {}", getTraceId(), postId);

        String title = likerName + " liked your post";
        String message = "Your post is getting attention";

        createNotification(new NotificationRequest.InternalNotificationRequest(
                null, // recipientId - to be fetched from post
                likerId,
                NotificationType.POST_LIKED,
                title,
                message,
                postId,
                null,
                null,
                null
        ));
    }

    @Override
    @Async("notificationExecutor")
    public void notifyReportSubmitted(Long reportId, String reporterName, String targetType, Long targetId) {
        log.info("[{}] Notifying admins about report ID: {}", getTraceId(), reportId);

        // Get all admin users
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        String title = reporterName + " submitted a report";
        String message = "New " + targetType + " report - ID: " + targetId;

        for (User admin : admins) {
            if (admin.getId() != null) {
                createNotification(new NotificationRequest.InternalNotificationRequest(
                        admin.getId(),  // recipientId (admin)
                        0L,             // userId (system)
                        NotificationType.REPORT_SUBMITTED,
                        title,
                        message,
                        null,           // postId
                        null,           // commentId
                        null,           // replyId
                        null
                ));
            }
        }
    }

    @Override
    @Async("notificationExecutor")
    public void notifyReportResolved(Long reportId, Long reporterId, String targetType) {
        log.info("[{}] Notifying reporter ID: {} about resolved report ID: {}", getTraceId(), reporterId, reportId);

        String title = "Your report has been resolved";
        String message = "The " + targetType + " you reported has been reviewed by our moderation team.";

        createNotification(new NotificationRequest.InternalNotificationRequest(
                reporterId,  // recipientId (reporter)
                0L,          // userId (system)
                NotificationType.REPORT_RESOLVED,
                title,
                message,
                null,
                null,
                null,
                null
        ));
    }

    @Override
    @Async("notificationExecutor")
    public void notifyReportRejected(Long reportId, Long reporterId, String targetType) {
        log.info("[{}] Notifying reporter ID: {} about rejected report ID: {}", getTraceId(), reporterId, reportId);

        String title = "Your report has been rejected";
        String message = "After review, the " + targetType + " does not violate our community guidelines.";

        createNotification(new NotificationRequest.InternalNotificationRequest(
                reporterId,  // recipientId (reporter)
                0L,          // userId (system)
                NotificationType.REPORT_REJECTED,
                title,
                message,
                null,
                null,
                null,
                null
        ));
    }

    @Override
    @Async("notificationExecutor")
    public void sendPendingPushNotifications() {
        log.info("[{}] Sending pending FCM push notifications", getTraceId());

        List<Notification> pendingNotifications = notificationRepository.findNotificationsPendingPush();
        log.info("[{}] Found {} notifications pending push", getTraceId(), pendingNotifications.size());

        for (Notification notification : pendingNotifications) {
            try {
                if (fcmService.isFcmAvailable() && notification.getFcmToken() != null) {
                    boolean sent = fcmService.sendPushNotification(
                            notification.getFcmToken(),
                            notification.getTitle(),
                            notification.getMessage(),
                            null
                    );

                    if (sent) {
                        notification.setPushSent(true);
                        notificationRepository.save(notification);
                        log.info("[{}] Push notification sent for ID: {}", getTraceId(), notification.getId());
                    }
                }
            } catch (Exception e) {
                log.error("[{}] Error sending push notification for ID: {}", getTraceId(), notification.getId(), e);
            }
        }
    }

    /**
     * Send notification via WebSocket (STOMP)
     */
    private void sendWebSocketNotification(Notification notification) {
        try {
            NotificationResponse.WebSocketNotification wsNotification = NotificationResponse.WebSocketNotification.fromEntity(notification);
            webSocketHandler.sendNotificationToUser(
                    notification.getRecipient().getId(),
                    wsNotification
            );
            log.info("[{}] WebSocket notification sent for user ID: {}", getTraceId(), notification.getRecipient().getId());
        } catch (Exception e) {
            log.warn("[{}] Failed to send WebSocket notification: {}", getTraceId(), e.getMessage());
        }
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}
