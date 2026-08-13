package com.umc.todayter.domain.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatchScheduler {

    private final NotificationDispatchService notificationDispatchService;

    @Scheduled(
            fixedDelayString = "${notification.dispatch.fixed-delay-ms:60000}",
            initialDelayString = "${notification.dispatch.initial-delay-ms:60000}"
    )
    public void dispatchNotifications() {
        runSafely("first-recommendation", notificationDispatchService::dispatchFirstRecommendations);
        runSafely("scheduled-reminders", notificationDispatchService::dispatchScheduledNotifications);
        runSafely("visit-record-reminders", notificationDispatchService::dispatchVisitRecordReminders);
    }

    private void runSafely(String jobName, Runnable job) {
        try {
            job.run();
        } catch (RuntimeException exception) {
            log.error("알림 발송 작업을 실패했습니다. job={}", jobName, exception);
        }
    }
}
