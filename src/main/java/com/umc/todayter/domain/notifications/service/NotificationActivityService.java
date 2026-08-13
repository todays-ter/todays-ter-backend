package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.domain.notifications.entity.NotificationActivity;
import com.umc.todayter.domain.notifications.entity.NotificationActivityType;
import com.umc.todayter.domain.notifications.repository.NotificationActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationActivityService {

    static final Duration VISIT_RECORD_REMIND_DELAY = Duration.ofHours(3);

    private final NotificationActivityRepository notificationActivityRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long memberId, Long placeId, NotificationActivityType activityType) {
        if (memberId == null || placeId == null || activityType == null) {
            return;
        }

        LocalDateTime occurredAt = LocalDateTime.now(clock);
        LocalDateTime dueAt = occurredAt.plus(VISIT_RECORD_REMIND_DELAY);
        NotificationActivity activity = notificationActivityRepository
                .findByUserIdAndPlaceId(memberId, placeId)
                .orElseGet(() -> notificationActivityRepository.save(
                        NotificationActivity.create(memberId, placeId, activityType, occurredAt, dueAt)
                ));
        activity.reschedule(activityType, occurredAt, dueAt);
    }
}
