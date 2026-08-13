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
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.SavedPlaceRepository;
import com.umc.todayter.domain.place.service.RecommendationMatchingService;
import com.umc.todayter.domain.record.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private static final int DUE_ACTIVITY_BATCH_SIZE = 100;

    private final MemberRepository memberRepository;
    private final FortuneReportRepository fortuneReportRepository;
    private final PlaceRepository placeRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final RecommendationMatchingService recommendationMatchingService;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationActivityRepository notificationActivityRepository;
    private final Clock clock;

    /**
     * 회원의 최초 완료 리포트가 존재하면 최고 점수 터 한 곳을 선정해 최초 1회만 알림을 생성한다.
     */
    @Transactional
    public void dispatchFirstRecommendations() {
        LocalDateTime now = LocalDateTime.now(clock);
        for (Member member : activeMembers()) {
            NotificationSetting setting = getOrCreateSetting(member.getId());
            if (!setting.isTodayRemind()) {
                continue;
            }

            String deduplicationKey = "FIRST_RECOMMENDATION:" + member.getId();
            if (notificationRepository.existsByDeduplicationKey(deduplicationKey)) {
                continue;
            }

            findTopPlace(member.getId(), placeRepository.findAllByActiveTrue())
                    .ifPresent(candidate -> createNotification(
                            member.getId(),
                            candidate.place().getId(),
                            "오늘 " + safeNickname(member) + "님과 잘 맞는 터를 찾았어요",
                            "오늘의 흐름과 고민에 맞는 추천 터를 확인해보세요.",
                            NotificationType.RECOMMENDATION,
                            now,
                            deduplicationKey
                    ));
        }
    }

    /**
     * 설정한 시각 이후, 주기가 도래한 회원에게 오늘 리마인드와 저장한 터 알림을 생성한다.
     */
    @Transactional
    public void dispatchScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();

        for (Member member : activeMembers()) {
            NotificationSetting setting = getOrCreateSetting(member.getId());
            setting.repairLegacyDefaults();
            if (now.toLocalTime().isBefore(setting.getRemindTime().getTime())) {
                continue;
            }

            dispatchTodayReminder(member, setting, today, now);
            dispatchSavedPlaceNotice(member, setting, today, now);
        }
    }

    /**
     * 추천 터를 보거나 저장한 당일의 사용자 설정 알림 시각에,
     * 해당 터의 방문 기록이 없으면 리마인드를 일괄 생성한다.
     */
    @Transactional
    public void dispatchVisitRecordReminders() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<NotificationActivity> activities = notificationActivityRepository.findDueActivities(
                now,
                PageRequest.of(0, DUE_ACTIVITY_BATCH_SIZE)
        );

        for (NotificationActivity activity : activities) {
            NotificationSetting setting = getOrCreateSetting(activity.getUserId());
            if (!isVisitRecordReminderEnabled(setting, activity.getActivityType())
                    || visitRecordRepository.existsByMemberIdAndPlaceId(
                    activity.getUserId(), activity.getPlaceId()
            )) {
                activity.markProcessed(now);
                continue;
            }

            Optional<Place> place = placeRepository.findByIdAndActiveTrue(activity.getPlaceId());
            if (place.isEmpty()) {
                activity.markProcessed(now);
                continue;
            }

            String deduplicationKey = "VISIT_RECORD_REMIND:" + activity.getId();
            if (!notificationRepository.existsByDeduplicationKey(deduplicationKey)) {
                createNotification(
                        activity.getUserId(),
                        activity.getPlaceId(),
                        "오늘 다녀온 터가 있나요?",
                        place.get().getName() + "에서의 경험을 기록으로 남겨보세요.",
                        NotificationType.VISIT_RECORD_REMIND,
                        now,
                        deduplicationKey
                );
            }
            activity.markProcessed(now);
        }
    }

    private void dispatchTodayReminder(
            Member member,
            NotificationSetting setting,
            LocalDate today,
            LocalDateTime now
    ) {
        if (!setting.isTodayReminderDue(today)) {
            return;
        }

        LocalDateTime startOfDay = today.atStartOfDay();
        boolean checkedToday = notificationActivityRepository
                .existsByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                        member.getId(), startOfDay, startOfDay.plusDays(1)
                );
        if (!checkedToday) {
            createNotification(
                    member.getId(),
                    null,
                    "오늘의 터, 아직 확인하지 않았어요",
                    "오늘 " + safeNickname(member) + "님과 잘 맞는 터를 만나보세요.",
                    NotificationType.TODAY_REMIND,
                    now,
                    "TODAY_REMIND:" + member.getId() + ":" + today
            );
        }
        setting.markTodayReminderSent(today);
    }

    private void dispatchSavedPlaceNotice(
            Member member,
            NotificationSetting setting,
            LocalDate today,
            LocalDateTime now
    ) {
        if (!setting.isSavedPlaceNoticeDue(today)) {
            return;
        }

        List<Place> savedPlaces = savedPlaceRepository
                .findAllByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(SavedPlace::getPlace)
                .filter(place -> Boolean.TRUE.equals(place.getActive()))
                .toList();
        if (savedPlaces.isEmpty()) {
            return;
        }

        findTopPlace(member.getId(), savedPlaces).ifPresent(candidate -> {
            createNotification(
                    member.getId(),
                    candidate.place().getId(),
                    "저장한 터가 오늘과 잘 어울려요",
                    candidate.place().getName() + "이(가) 오늘의 흐름과 잘 맞아요.",
                    NotificationType.SAVED_PLACE,
                    now,
                    "SAVED_PLACE:" + member.getId() + ":" + today
            );
            setting.markSavedPlaceNoticeSent(today);
        });
    }

    private Optional<RankedPlace> findTopPlace(Long memberId, List<Place> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        try {
            Optional<FortuneReport> report = fortuneReportRepository
                    .findFirstByMemberIdAndStatusOrderByIdDesc(memberId, FortuneReportStatus.COMPLETED);
            if (report.isEmpty()) {
                return Optional.empty();
            }
            Optional<RecommendationScoringContext> scoringContext = recommendationMatchingService
                    .prepare(report.get());
            if (scoringContext.isEmpty()) {
                return Optional.empty();
            }

            return candidates.stream()
                    .map(place -> {
                        RecommendationMatchContext match = recommendationMatchingService
                                .score(scoringContext.get(), place);
                        return new RankedPlace(place, match.totalScore());
                    })
                    .max(Comparator
                            .comparingInt(RankedPlace::score)
                            .thenComparing(
                                    candidate -> candidate.place().getAverageRating(),
                                    Comparator.nullsFirst(Comparator.naturalOrder())
                            )
                            .thenComparing(candidate -> -candidate.place().getId()));
        } catch (RuntimeException exception) {
            log.warn("알림용 추천 점수를 계산하지 못했습니다. memberId={}", memberId, exception);
            return Optional.empty();
        }
    }

    private List<Member> activeMembers() {
        return memberRepository.findAll().stream()
                .filter(Member::isActive)
                .toList();
    }

    private NotificationSetting getOrCreateSetting(Long memberId) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(memberId)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.createDefault(memberId)
                ));
        setting.repairLegacyDefaults();
        return setting;
    }

    private boolean isVisitRecordReminderEnabled(
            NotificationSetting setting,
            NotificationActivityType activityType
    ) {
        return activityType == NotificationActivityType.PLACE_SAVED
                ? setting.isSavedPlace()
                : setting.isTodayRemind();
    }

    private void createNotification(
            Long memberId,
            Long placeId,
            String title,
            String content,
            NotificationType type,
            LocalDateTime now,
            String deduplicationKey
    ) {
        if (notificationRepository.existsByDeduplicationKey(deduplicationKey)) {
            return;
        }
        notificationRepository.save(Notification.create(
                memberId,
                placeId,
                title,
                content,
                type,
                now,
                deduplicationKey
        ));
    }

    private String safeNickname(Member member) {
        return member.getNickname() == null || member.getNickname().isBlank()
                ? "회원"
                : member.getNickname();
    }

    private record RankedPlace(Place place, int score) {
    }
}
