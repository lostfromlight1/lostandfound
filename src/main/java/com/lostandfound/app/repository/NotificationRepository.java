package com.lostandfound.app.repository;

import com.lostandfound.app.model.Notification;
import com.lostandfound.app.model.NotificationStatus;
import com.lostandfound.app.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n FROM Notification n
        WHERE n.recipient.id = :recipientId AND n.active = true
        ORDER BY n.createdAt DESC
    """)
    Page<Notification> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("""
        SELECT COUNT(n) FROM Notification n
        WHERE n.recipient.id = :recipientId 
        AND n.status = 'UNREAD' 
        AND n.active = true
    """)
    long countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    @Query("""
        SELECT n FROM Notification n
        WHERE n.recipient.id = :recipientId 
        AND n.status = 'UNREAD' 
        AND n.active = true
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findUnreadByRecipientId(@Param("recipientId") Long recipientId);

    @Query("""
        SELECT n FROM Notification n
        WHERE n.recipient.id = :recipientId 
        AND n.type = :type 
        AND n.active = true
        ORDER BY n.createdAt DESC
    """)
    Page<Notification> findByRecipientIdAndType(
            @Param("recipientId") Long recipientId,
            @Param("type") NotificationType type,
            Pageable pageable
    );


    @Query("""
        SELECT n FROM Notification n
        WHERE n.postId = :postId 
        AND n.active = true
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findByPostId(@Param("postId") Long postId);


    @Query("""
        SELECT n FROM Notification n
        WHERE n.commentId = :commentId 
        AND n.active = true
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findByCommentId(@Param("commentId") Long commentId);


    @Query("""
        SELECT n FROM Notification n
        WHERE n.replyId = :replyId 
        AND n.active = true
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findByReplyId(@Param("replyId") Long replyId);


    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP
        WHERE n.recipient.id = :recipientId 
        AND n.status = 'UNREAD' 
        AND n.active = true
    """)
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);


    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.active = false, n.deletedAt = CURRENT_TIMESTAMP
        WHERE n.recipient.id = :recipientId 
        AND n.createdAt < :beforeDate
    """)
    int deleteOldNotifications(
            @Param("recipientId") Long recipientId,
            @Param("beforeDate") LocalDateTime beforeDate
    );


    @Query("""
        SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END
        FROM Notification n
        WHERE n.recipient.id = :recipientId 
        AND n.user.id = :userId 
        AND n.type = :type 
        AND n.postId = :postId
        AND n.status = 'UNREAD'
        AND n.active = true
    """)
    boolean existsByRecipientAndUserAndTypeAndPost(
            @Param("recipientId") Long recipientId,
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            @Param("postId") Long postId
    );

    @Query(value = """
        SELECT * FROM notifications n
        WHERE n.push_sent = false 
        AND n.fcm_token IS NOT NULL 
        AND n.active = true
        LIMIT 100
    """, nativeQuery = true)
    List<Notification> findNotificationsPendingPush();


    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId 
        AND n.active = true
        ORDER BY n.createdAt DESC
    """)
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
