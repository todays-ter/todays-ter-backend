package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.notifications.entity.Notification;
import com.umc.todayter.domain.notifications.entity.NotificationActivity;
import com.umc.todayter.domain.notifications.entity.NotificationActivityType;
import com.umc.todayter.domain.notifications.entity.NotificationSetting;
import com.umc.todayter.domain.notifications.entity.NotificationType;
import com.umc.todayter.domain.notifications.repository.NotificationActivityRepository;
import com.umc.todayter.domain.notifications.repository.NotificationRepository;
import com.umc.todayter.domain.notifications.repository.NotificationSettingRepository;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.dto.internal.RecommendationScoringContext;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.SavedPlace;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.SavedPlaceRepository;
import com.umc.todayter.domain.place.service.RecommendationMatchingService;
import com.umc.todayter.domain.record.repository.VisitRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock private MemberRepository memberRepository;
    @Mock private FortuneReportRepository fortuneReportRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private SavedPlaceRepository savedPlaceRepository;
    @Mock private VisitRecordRepository visitRecordRepository;
    @Mock private RecommendationMatchingService recommendationMatchingService;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationSettingRepository notificationSettingRepository;
    @Mock private NotificationActivityRepository notificationActivityRepository;

    private NotificationDispatchService service;
    private Member member;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-13T09:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        service = new NotificationDispatchService(
                memberRepository,
                fortuneReportRepository,
                placeRepository,
                savedPlaceRepository,
                visitRecordRepository,
                recommendationMatchingService,
                notificationRepository,
                notificationSettingRepository,
                notificationActivityRepository,
                clock
        );
        member = Member.create("오늘이");
        ReflectionTestUtils.setField(member, "id", MEMBER_ID);
    }

    @Test
    void savedPlaceNoticeChoosesOnlyHighestScoringPlace() {
        NotificationSetting setting = NotificationSetting.createDefault(MEMBER_ID);
        setting.updateSettings(false, null, null, true, null, null);
        Place lower = place(10L, "낮은 점수 터");
        Place highest = place(20L, "최고 점수 터");
        SavedPlace lowerSaved = mock(SavedPlace.class);
        SavedPlace highestSaved = mock(SavedPlace.class);
        FortuneReport report = mock(FortuneReport.class);
        RecommendationScoringContext scoringContext = new RecommendationScoringContext(
                100L, null, ElementType.WOOD, ElementType.FIRE, List.of()
        );

        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(notificationSettingRepository.findByUserId(MEMBER_ID)).thenReturn(Optional.of(setting));
        when(savedPlaceRepository.findAllByMemberIdOrderByCreatedAtDesc(MEMBER_ID))
                .thenReturn(List.of(lowerSaved, highestSaved));
        when(lowerSaved.getPlace()).thenReturn(lower);
        when(highestSaved.getPlace()).thenReturn(highest);
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                MEMBER_ID, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(recommendationMatchingService.prepare(report)).thenReturn(Optional.of(scoringContext));
        when(recommendationMatchingService.score(scoringContext, lower)).thenReturn(score(50));
        when(recommendationMatchingService.score(scoringContext, highest)).thenReturn(score(90));
        when(notificationRepository.existsByDeduplicationKey(anyString())).thenReturn(false);

        service.dispatchScheduledNotifications();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.SAVED_PLACE);
        assertThat(captor.getValue().getPlaceId()).isEqualTo(20L);
        assertThat(captor.getValue().getContent()).contains("최고 점수 터");
    }

    @Test
    void dueActivityCreatesVisitRecordReminderAfterThreeHours() {
        NotificationActivity activity = NotificationActivity.create(
                MEMBER_ID,
                10L,
                NotificationActivityType.RECOMMENDATION_VIEWED,
                java.time.LocalDateTime.of(2026, 8, 13, 12, 0),
                java.time.LocalDateTime.of(2026, 8, 13, 15, 0)
        );
        ReflectionTestUtils.setField(activity, "id", 77L);
        Place place = place(10L, "기록할 터");

        when(notificationActivityRepository.findDueActivities(any(), any()))
                .thenReturn(List.of(activity));
        when(notificationSettingRepository.findByUserId(MEMBER_ID))
                .thenReturn(Optional.of(NotificationSetting.createDefault(MEMBER_ID)));
        when(visitRecordRepository.existsByMemberIdAndPlaceId(MEMBER_ID, 10L)).thenReturn(false);
        when(placeRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(place));
        when(notificationRepository.existsByDeduplicationKey(anyString())).thenReturn(false);

        service.dispatchVisitRecordReminders();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.VISIT_RECORD_REMIND);
        assertThat(captor.getValue().getPlaceId()).isEqualTo(10L);
        assertThat(activity.getProcessedAt()).isNotNull();
    }

    @Test
    void existingVisitRecordSuppressesReminder() {
        NotificationActivity activity = NotificationActivity.create(
                MEMBER_ID,
                10L,
                NotificationActivityType.PLACE_SAVED,
                java.time.LocalDateTime.of(2026, 8, 13, 12, 0),
                java.time.LocalDateTime.of(2026, 8, 13, 15, 0)
        );
        when(notificationActivityRepository.findDueActivities(any(), any()))
                .thenReturn(List.of(activity));
        when(notificationSettingRepository.findByUserId(MEMBER_ID))
                .thenReturn(Optional.of(NotificationSetting.createDefault(MEMBER_ID)));
        when(visitRecordRepository.existsByMemberIdAndPlaceId(MEMBER_ID, 10L)).thenReturn(true);

        service.dispatchVisitRecordReminders();

        verify(notificationRepository, never()).save(any(Notification.class));
        assertThat(activity.getProcessedAt()).isNotNull();
    }

    private Place place(Long id, String name) {
        Place place = Place.builder()
                .name(name)
                .active(true)
                .build();
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }

    private RecommendationMatchContext score(int totalScore) {
        return new RecommendationMatchContext(
                100L,
                null,
                ElementType.WOOD,
                ElementType.FIRE,
                List.of(),
                totalScore,
                0,
                0
        );
    }
}
