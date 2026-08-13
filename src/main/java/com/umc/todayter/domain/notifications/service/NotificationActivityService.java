package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.domain.notifications.entity.NotificationActivity;
import com.umc.todayter.domain.notifications.entity.NotificationActivityType;
import com.umc.todayter.domain.notifications.entity.NotificationSetting;
import com.umc.todayter.domain.notifications.repository.NotificationActivityRepository;
import com.umc.todayter.domain.notifications.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationActivityService {

    private final NotificationActivityRepository notificationActivityRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long memberId, Long placeId, NotificationActivityType activityType) {
        if (memberId == null || placeId == null || activityType == null) {
            return;
        }

        LocalDateTime occurredAt = LocalDateTime.now(clock);
        LocalDateTime dueAt = resolveDueAt(memberId, occurredAt);
        NotificationActivity activity = notificationActivityRepository
                .findByUserIdAndPlaceId(memberId, placeId)
                .orElseGet(() -> notificationActivityRepository.save(
                        NotificationActivity.create(memberId, placeId, activityType, occurredAt, dueAt)
                ));
        activity.reschedule(activityType, occurredAt, dueAt);
    }

    private LocalDateTime resolveDueAt(Long memberId, LocalDateTime occurredAt) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(memberId)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.createDefault(memberId)
                ));
        setting.repairLegacyDefaults();

        LocalDateTime configuredTime = occurredAt.toLocalDate()
                .atTime(setting.getRemindTime().getTime());
        return configuredTime.isBefore(occurredAt) ? occurredAt : configuredTime;
    }
}
