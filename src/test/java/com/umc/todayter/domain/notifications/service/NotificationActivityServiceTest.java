package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.domain.notifications.entity.NotificationActivity;
import com.umc.todayter.domain.notifications.entity.NotificationActivityType;
import com.umc.todayter.domain.notifications.repository.NotificationActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationActivityServiceTest {

    @Mock
    private NotificationActivityRepository notificationActivityRepository;

    @Test
    void recommendationViewSchedulesVisitRecordReminderThreeHoursLater() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-13T03:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        NotificationActivityService service = new NotificationActivityService(
                notificationActivityRepository,
                clock
        );
        when(notificationActivityRepository.findByUserIdAndPlaceId(1L, 10L))
                .thenReturn(Optional.empty());
        when(notificationActivityRepository.save(any(NotificationActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.record(1L, 10L, NotificationActivityType.RECOMMENDATION_VIEWED);

        ArgumentCaptor<NotificationActivity> captor = ArgumentCaptor.forClass(NotificationActivity.class);
        verify(notificationActivityRepository).save(captor.capture());
        assertThat(captor.getValue().getOccurredAt())
                .isEqualTo(LocalDateTime.of(2026, 8, 13, 12, 0));
        assertThat(captor.getValue().getDueAt())
                .isEqualTo(LocalDateTime.of(2026, 8, 13, 15, 0));
    }
}
