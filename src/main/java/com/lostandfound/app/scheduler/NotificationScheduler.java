package com.lostandfound.app.scheduler;

import com.lostandfound.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Notification Scheduler
 * Handles periodic notification tasks like sending pending FCM notifications
 */
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Send pending FCM push notifications every 5 minutes
     * This ensures notifications are delivered even if FCM was temporarily unavailable
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000) // 5 minutes, 1 minute initial delay
    public void sendPendingPushNotifications() {
        log.info("Starting scheduled task: sending pending push notifications");
        try {
            notificationService.sendPendingPushNotifications();
            log.info("Completed scheduled task: sending pending push notifications");
        } catch (Exception e) {
            log.error("Error in scheduled task for sending pending push notifications", e);
        }
    }

    /**
     * Clean up old notifications every day at 2 AM
     * Keeps database size manageable
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM every day
    public void cleanupOldNotifications() {
        log.info("Starting scheduled task: cleanup old notifications");
        try {
            // Implement cleanup logic
            log.info("Completed scheduled task: cleanup old notifications");
        } catch (Exception e) {
            log.error("Error in scheduled task for cleanup", e);
        }
    }
}
