package com.lostandfound.app.scheduler;

import com.lostandfound.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;


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

    @Scheduled(cron = "0 0 2 * * ?") // 2 AM every day
    public void cleanupOldNotifications() {
        log.info(" Starting scheduled task: cleanup old notifications");
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            log.info(" Deleting notifications older than: {}", thirtyDaysAgo);

            int deletedCount = 0;

            deletedCount = notificationService.deleteOldNotifications(thirtyDaysAgo);

            log.info(" Completed scheduled task: cleanup old notifications");
            log.info("Statistics: {} old notifications deleted", deletedCount);

        } catch (Exception e) {
            log.error(" Error in scheduled task for cleanup", e);
        }
    }
}
