package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.dto.request.NotificationRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.NotificationResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.model.NotificationType;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "6. Notification Management", description = "Endpoints for managing notifications (comments, replies, likes)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-001")
    @Operation(summary = "Get Notifications", description = "Fetches paginated notifications for the authenticated user.")
    public ResponseEntity<BaseResponse<PageResponse<NotificationResponse.NotificationSummary>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to fetch notifications for user ID: {}", userDetails.getId());
        PageResponse<NotificationResponse.NotificationSummary> response = notificationService.getNotifications(
                userDetails.getId(), page, size
        );
        return BaseResponse.success("Notifications fetched successfully", response);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-002")
    @Operation(summary = "Get Notifications by Type", description = "Fetches notifications filtered by type.")
    public ResponseEntity<BaseResponse<PageResponse<NotificationResponse.NotificationSummary>>> getNotificationsByType(
            @PathVariable NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to fetch notifications of type {} for user ID: {}", type, userDetails.getId());
        PageResponse<NotificationResponse.NotificationSummary> response = notificationService.getNotificationsByType(
                userDetails.getId(), type, page, size
        );
        return BaseResponse.success("Notifications fetched successfully", response);
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-003")
    @Operation(summary = "Get Unread Count", description = "Gets the count of unread notifications for the user.")
    public ResponseEntity<BaseResponse<NotificationResponse.UnreadCountDto>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to get unread notification count for user ID: {}", userDetails.getId());
        NotificationResponse.UnreadCountDto response = notificationService.getUnreadCount(userDetails.getId());
        return BaseResponse.success("Unread count fetched successfully", response);
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-004")
    @Operation(summary = "Get Unread Notifications", description = "Fetches all unread notifications for the user.")
    public ResponseEntity<BaseResponse<List<NotificationResponse.NotificationDto>>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to fetch unread notifications for user ID: {}", userDetails.getId());
        List<NotificationResponse.NotificationDto> response = notificationService.getUnreadNotifications(
                userDetails.getId()
        );
        return BaseResponse.success("Unread notifications fetched successfully", response);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-005")
    @Operation(summary = "Mark as Read", description = "Marks a specific notification as read.")
    public ResponseEntity<BaseResponse<NotificationResponse.NotificationActionResponse>> markAsRead(
            @PathVariable Long notificationId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to mark notification ID: {} as read by user ID: {}", notificationId, userDetails.getId());
        NotificationResponse.NotificationActionResponse response = notificationService.markAsRead(
                notificationId, userDetails
        );
        return BaseResponse.success("Notification marked as read", response);
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-006")
    @Operation(summary = "Mark All as Read", description = "Marks all notifications as read for the user.")
    public ResponseEntity<BaseResponse<NotificationResponse.NotificationActionResponse>> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to mark all notifications as read for user ID: {}", userDetails.getId());
        NotificationResponse.NotificationActionResponse response = notificationService.markAllAsRead(userDetails);
        return BaseResponse.success("All notifications marked as read", response);
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-007")
    @Operation(summary = "Delete Notification", description = "Deletes a specific notification.")
    public ResponseEntity<BaseResponse<NotificationResponse.NotificationActionResponse>> deleteNotification(
            @PathVariable Long notificationId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to delete notification ID: {} by user ID: {}", notificationId, userDetails.getId());
        NotificationResponse.NotificationActionResponse response = notificationService.deleteNotification(
                notificationId, userDetails
        );
        return BaseResponse.success("Notification deleted", response);
    }

    @PostMapping("/fcm-token")
    @PreAuthorize("isAuthenticated()")
    @ApiId("NOT-008")
    @Operation(summary = "Save FCM Token", description = "Saves FCM device token for push notifications.")
    public ResponseEntity<BaseResponse<NotificationResponse.FcmTokenResponse>> saveFcmToken(
            @Valid @RequestBody NotificationRequest.SaveFcmTokenRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to save FCM token for user ID: {}", userDetails.getId());
        NotificationResponse.FcmTokenResponse response = notificationService.saveFcmToken(
                userDetails.getId(), request.fcmToken()
        );
        return BaseResponse.success("FCM token saved successfully", response);
    }
}
