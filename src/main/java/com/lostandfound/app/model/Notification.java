package com.lostandfound.app.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_recipient_id", columnList = "recipient_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_recipient_status", columnList = "recipient_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who triggered the notification (actor)
     * e.g., User who commented, liked, replied
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The user who receives the notification (recipient)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /**
     * Type of notification
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    /**
     * Status of notification (READ/UNREAD)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;

    /**
     * Title/subject of notification
     */
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    /**
     * Message/body of notification
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Related post ID (if notification is related to a post)
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * Related comment ID (if notification is related to a comment)
     */
    @Column(name = "comment_id")
    private Long commentId;

    /**
     * Related reply ID (if notification is related to a reply)
     */
    @Column(name = "reply_id")
    private Long replyId;

    /**
     * FCM token for push notification
     */
    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    /**
     * Whether push notification was sent
     */
    @Column(name = "push_sent")
    @Builder.Default
    private Boolean pushSent = false;

    /**
     * Timestamp when notification was read
     */
    @Column(name = "read_at")
    private java.time.LocalDateTime readAt;

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = java.time.LocalDateTime.now();
    }

    /**
     * Mark notification as unread
     */
    public void markAsUnread() {
        this.status = NotificationStatus.UNREAD;
        this.readAt = null;
    }
}
