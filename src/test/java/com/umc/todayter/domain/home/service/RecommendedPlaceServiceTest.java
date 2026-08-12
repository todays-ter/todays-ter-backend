package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.home.dto.request.HomeRecommendedPlaceQuery;
import com.umc.todayter.domain.home.dto.response.HomeRecommendedPlacesResponse;
import com.umc.todayter.domain.home.exception.HomeErrorCode;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.dto.internal.RecommendationScoringContext;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.service.PlaceDistanceCalculator;
import com.umc.todayter.domain.place.service.PlaceRecommendationSnapshotService;
import com.umc.todayter.domain.place.service.PlaceThumbnailUrlFactory;
import com.umc.todayter.domain.place.service.RecommendationMatchingService;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendedPlaceServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FortuneReportRepository fortuneReportRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private RecommendationMatchingService recommendationMatchingService;

    @Mock
    private PlaceRecommendationSnapshotService snapshotService;

    @Mock
    private com.umc.todayter.domain.record.repository.VisitRecordRepository visitRecordRepository;

    @Test
    void memberReturnsTopThreeFromAllActivePlaces() {
        FortuneReport report = report(99L, 7L, FortuneReportStatus.COMPLETED, "content");
        RecommendationScoringContext scoringContext = scoringContext(report.getId());
        List<Place> places = List.of(
                place(10L, "score-first", ElementType.EARTH, 4.0, "google-10"),
                place(1L, "rating-first", ElementType.FIRE, 5.0, "google-1"),
                place(3L, "id-first", ElementType.WOOD, 4.8, null),
                place(9L, "id-second", ElementType.WATER, 4.8, "google-9")
        );

        when(memberRepository.findById(1L)).thenReturn(Optional.of(Member.create("member")));
        when(fortuneReportRepository.findFirstByMemberIdOrderByCreatedAtDescIdDesc(1L))
                .thenReturn(Optional.of(report));
        when(recommendationMatchingService.prepare(report)).thenReturn(Optional.of(scoringContext));
        when(placeRepository.findAllByActiveTrue()).thenReturn(places);
        score(scoringContext, places.get(0), 95);
        score(scoringContext, places.get(1), 90);
        score(scoringContext, places.get(2), 90);
        score(scoringContext, places.get(3), 90);
        snapshot(places.get(0), "reason-10");
        snapshot(places.get(1), "reason-1");
        snapshot(places.get(2), "reason-3");

        HomeRecommendedPlacesResponse response = service().getRecommendedPlaces(
                CurrentUserContext.forMember(1L),
                HomeRecommendedPlaceQuery.of(37.5665, 126.9780),
                "http://localhost:8080"
        );

        assertThat(response.userType()).isEqualTo(CurrentUserType.MEMBER);
        assertThat(response.isLimited()).isFalse();
        assertThat(response.totalCount()).isEqualTo(4);
        assertThat(response.visibleCount()).isEqualTo(3);
        assertThat(response.loginPrompt()).isNull();
        assertThat(response.recommendations()).extracting("placeId").containsExactly(10L, 1L, 3L);
        assertThat(response.recommendations()).extracting("rankOrder").containsExactly(1, 2, 3);
        assertThat(response.recommendations().get(0).thumbnailUrl())
                .isEqualTo("http://localhost:8080/places/10/thumbnail");
        assertThat(response.recommendations().get(2).thumbnailUrl()).isNull();
        assertThat(response.recommendations().get(0).recommendationReason()).isEqualTo("reason-10");
        assertThat(response.recommendations().get(0).distanceKm()).isNotNull();
        verify(snapshotService, never()).getOrCreate(
                org.mockito.ArgumentMatchers.any(RecommendationMatchContext.class),
                org.mockito.ArgumentMatchers.eq(places.get(3))
        );
    }

    @Test
    void guestReturnsTopOneAndLoginPrompt() {
        FortuneReport report = report(100L, 8L, FortuneReportStatus.COMPLETED, "content");
        RecommendationScoringContext scoringContext = scoringContext(report.getId());
        Place first = place(1L, "first", ElementType.FIRE, 4.0, "google");
        Place second = place(2L, "second", ElementType.WOOD, 5.0, "google");

        when(fortuneReportRepository.findFirstByGuestSessionIdOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(Optional.of(report));
        when(recommendationMatchingService.prepare(report)).thenReturn(Optional.of(scoringContext));
        when(placeRepository.findAllByActiveTrue()).thenReturn(List.of(first, second));
        score(scoringContext, first, 80);
        score(scoringContext, second, 70);
        snapshot(first, "guest reason");

        HomeRecommendedPlacesResponse response = service().getRecommendedPlaces(
                CurrentUserContext.forGuest(10L, "guest-id"),
                HomeRecommendedPlaceQuery.of(null, null),
                "http://localhost:8080"
        );

        assertThat(response.userType()).isEqualTo(CurrentUserType.GUEST);
        assertThat(response.isLimited()).isTrue();
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.visibleCount()).isEqualTo(1);
        assertThat(response.recommendations()).extracting("placeId").containsExactly(1L);
        assertThat(response.recommendations().get(0).distanceKm()).isNull();
        assertThat(response.loginPrompt().title()).isEqualTo("로그인 후 더 많은 터를 탐색해보세요");
        verifyNoInteractions(memberRepository);
        verify(snapshotService, never()).getOrCreate(
                org.mockito.ArgumentMatchers.any(RecommendationMatchContext.class),
                org.mockito.ArgumentMatchers.eq(second)
        );
    }

    @Test
    void averageRatingUsesReviewAverageWhenPresentOtherwiseNull() {
        FortuneReport report = report(99L, 7L, FortuneReportStatus.COMPLETED, "content");
        RecommendationScoringContext scoringContext = scoringContext(report.getId());
        Place reviewed = place(1L, "reviewed", ElementType.FIRE, 4.0, "google");
        Place unreviewed = place(2L, "unreviewed", ElementType.WOOD, 3.5, "google");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(Member.create("member")));
        when(fortuneReportRepository.findFirstByMemberIdOrderByCreatedAtDescIdDesc(1L))
                .thenReturn(Optional.of(report));
        when(recommendationMatchingService.prepare(report)).thenReturn(Optional.of(scoringContext));
        when(placeRepository.findAllByActiveTrue()).thenReturn(List.of(reviewed, unreviewed));
        score(scoringContext, reviewed, 80);
        score(scoringContext, unreviewed, 70);
        snapshot(reviewed, "reviewed reason");
        snapshot(unreviewed, "unreviewed reason");
        when(visitRecordRepository.findAverageRatingByPlaceId(1L)).thenReturn(4.8);
        when(visitRecordRepository.findAverageRatingByPlaceId(2L)).thenReturn(null);

        HomeRecommendedPlacesResponse response = service().getRecommendedPlaces(
                CurrentUserContext.forMember(1L),
                HomeRecommendedPlaceQuery.of(null, null),
                "http://localhost:8080"
        );

        assertThat(response.recommendations().get(0).averageRating()).isEqualTo(4.8);
        assertThat(response.recommendations().get(1).averageRating()).isNull();
    }

    @Test
    void emptyActivePlacesReturnsEmptyWithoutSnapshot() {
        FortuneReport report = report(100L, 8L, FortuneReportStatus.COMPLETED, "content");
        RecommendationScoringContext scoringContext = scoringContext(report.getId());
        when(fortuneReportRepository.findFirstByGuestSessionIdOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(Optional.of(report));
        when(recommendationMatchingService.prepare(report)).thenReturn(Optional.of(scoringContext));
        when(placeRepository.findAllByActiveTrue()).thenReturn(List.of());

        HomeRecommendedPlacesResponse response = service().getRecommendedPlaces(
                CurrentUserContext.forGuest(10L, "guest-id"),
                HomeRecommendedPlaceQuery.of(null, null),
                "http://localhost:8080"
        );

        assertThat(response.totalCount()).isZero();
        assertThat(response.visibleCount()).isZero();
        assertThat(response.recommendations()).isEmpty();
        assertThat(response.loginPrompt()).isNotNull();
        verifyNoInteractions(snapshotService);
    }

    @Test
    void missingOrInactiveMemberStopsBeforeRecommendationWork() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertError(
                () -> service().getRecommendedPlaces(
                        CurrentUserContext.forMember(1L),
                        HomeRecommendedPlaceQuery.of(null, null),
                        "http://localhost"
                ),
                MemberErrorCode.MEMBER_NOT_FOUND
        );
        verifyNoInteractions(fortuneReportRepository, placeRepository, recommendationMatchingService, snapshotService);

        Member inactive = Member.create("inactive");
        ReflectionTestUtils.setField(inactive, "status", MemberStatus.WITHDRAWN);
        when(memberRepository.findById(2L)).thenReturn(Optional.of(inactive));

        assertError(
                () -> service().getRecommendedPlaces(
                        CurrentUserContext.forMember(2L),
                        HomeRecommendedPlaceQuery.of(null, null),
                        "http://localhost"
                ),
                MemberErrorCode.MEMBER_INACTIVE
        );
    }

    @Test
    void latestReportStatusDeterminesHomeError() {
        when(fortuneReportRepository.findFirstByGuestSessionIdOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(Optional.empty());
        assertError(
                () -> guestCall(),
                HomeErrorCode.FORTUNE_REPORT_NOT_FOUND
        );

        when(fortuneReportRepository.findFirstByGuestSessionIdOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(Optional.of(report(1L, 1L, FortuneReportStatus.PROCESSING, "content")));
        assertError(
                () -> guestCall(),
                HomeErrorCode.FORTUNE_REPORT_PROCESSING
        );

        when(fortuneReportRepository.findFirstByGuestSessionIdOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(Optional.of(report(2L, 1L, FortuneReportStatus.FAILED, "content")));
        assertError(
                () -> guestCall(),
                HomeErrorCode.FORTUNE_REPORT_NOT_FOUND
        );
    }

    private void guestCall() {
        service().getRecommendedPlaces(
                CurrentUserContext.forGuest(10L, "guest-id"),
                HomeRecommendedPlaceQuery.of(null, null),
                "http://localhost"
        );
    }

    private void score(RecommendationScoringContext scoringContext, Place place, int totalScore) {
        RecommendationMatchContext matchContext = new RecommendationMatchContext(
                scoringContext.reportId(),
                scoringContext.basicReport(),
                scoringContext.neededElement(),
                scoringContext.dailyElement(),
                scoringContext.concerns(),
                totalScore,
                0,
                0
        );
        when(recommendationMatchingService.score(scoringContext, place)).thenReturn(matchContext);
    }

    private void snapshot(Place place, String reason) {
        PlaceRecommendationSnapshot snapshot = org.mockito.Mockito.mock(PlaceRecommendationSnapshot.class);
        when(snapshot.getWhyItMatches()).thenReturn(reason);
        when(snapshotService.getOrCreate(
                org.mockito.ArgumentMatchers.any(RecommendationMatchContext.class),
                org.mockito.ArgumentMatchers.eq(place)
        )).thenReturn(snapshot);
    }

    private void assertError(Runnable action, Object errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(errorCode));
    }

    private RecommendedPlaceService service() {
        return new RecommendedPlaceService(
                memberRepository,
                fortuneReportRepository,
                placeRepository,
                recommendationMatchingService,
                snapshotService,
                new PlaceDistanceCalculator(),
                new PlaceThumbnailUrlFactory(),
                visitRecordRepository
        );
    }

    private RecommendationScoringContext scoringContext(Long reportId) {
        return new RecommendationScoringContext(
                reportId,
                org.mockito.Mockito.mock(BasicReport.class),
                ElementType.FIRE,
                ElementType.WOOD,
                List.of()
        );
    }

    private FortuneReport report(Long id, Long onboardingId, FortuneReportStatus status, String reportContent) {
        try {
            Constructor<FortuneReport> constructor = FortuneReport.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FortuneReport report = constructor.newInstance();
            ReflectionTestUtils.setField(report, "id", id);
            ReflectionTestUtils.setField(report, "onboardingId", onboardingId);
            ReflectionTestUtils.setField(report, "status", status);
            ReflectionTestUtils.setField(report, "reportContent", reportContent);
            return report;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Place place(Long id, String name, ElementType elementType, double averageRating, String googlePlaceId) {
        Place place = Place.builder()
                .name(name)
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5796)
                .longitude(126.9770)
                .elementType(elementType)
                .themeType(ThemeType.LOVE)
                .averageRating(averageRating)
                .reviewCount(0)
                .editorPick(false)
                .active(true)
                .googlePlaceId(googlePlaceId)
                .terrainType("terrain")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(0)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }
}
