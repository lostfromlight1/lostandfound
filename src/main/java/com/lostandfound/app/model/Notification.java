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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;


    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;


    @Column(name = "title", length = 255, nullable = false)
    private String title;


    @Column(name = "message", columnDefinition = "TEXT")
    private String message;


    @Column(name = "post_id")
    private Long postId;


    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "reply_id")
    private Long replyId;


    @Column(name = "fcm_token", length = 500)
    private String fcmToken;


    @Column(name = "push_sent")
    @Builder.Default
    private Boolean pushSent = false;

    @Column(name = "read_at")
    private java.time.LocalDateTime readAt;

    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = java.time.LocalDateTime.now();
    }

    public void markAsUnread() {
        this.status = NotificationStatus.UNREAD;
        this.readAt = null;
    }
}
