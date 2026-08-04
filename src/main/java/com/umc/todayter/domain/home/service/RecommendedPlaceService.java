package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.home.dto.request.HomeRecommendedPlaceQuery;
import com.umc.todayter.domain.home.dto.response.HomeLoginPromptResponse;
import com.umc.todayter.domain.home.dto.response.HomeRecommendedPlaceItemResponse;
import com.umc.todayter.domain.home.dto.response.HomeRecommendedPlacesResponse;
import com.umc.todayter.domain.home.exception.HomeErrorCode;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.dto.internal.RecommendationScoringContext;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.service.PlaceDistanceCalculator;
import com.umc.todayter.domain.place.service.PlaceRecommendationSnapshotService;
import com.umc.todayter.domain.place.service.PlaceThumbnailUrlFactory;
import com.umc.todayter.domain.place.service.RecommendationMatchingService;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RecommendedPlaceService {

    private static final int MEMBER_LIMIT = 3;
    private static final int GUEST_LIMIT = 1;

    private final MemberRepository memberRepository;
    private final FortuneReportRepository fortuneReportRepository;
    private final PlaceRepository placeRepository;
    private final RecommendationMatchingService recommendationMatchingService;
    private final PlaceRecommendationSnapshotService snapshotService;
    private final PlaceDistanceCalculator distanceCalculator;
    private final PlaceThumbnailUrlFactory thumbnailUrlFactory;

    public HomeRecommendedPlacesResponse getRecommendedPlaces(
            CurrentUserContext context,
            HomeRecommendedPlaceQuery query,
            String contextPathUrl
    ) {
        FortuneReport report = context.isMember()
                ? getMemberLatestReport(context.memberId())
                : getGuestLatestReport(context.guestSessionId());
        validateCompleted(report);

        RecommendationScoringContext scoringContext = recommendationMatchingService.prepare(report)
                .orElseThrow(() -> new IllegalStateException("Recommendation scoring context is unavailable."));

        List<RankedPlaceCandidate> rankedCandidates = rank(placeRepository.findAllByActiveTrue(), scoringContext);
        int totalCount = rankedCandidates.size();
        int limit = context.isMember() ? MEMBER_LIMIT : GUEST_LIMIT;

        List<HomeRecommendedPlaceItemResponse> recommendations = rankedCandidates.stream()
                .limit(limit)
                .map(candidate -> toItemResponse(candidate, query, contextPathUrl))
                .toList();

        return new HomeRecommendedPlacesResponse(
                context.userType(),
                context.isGuest(),
                recommendations.size(),
                totalCount,
                recommendations,
                context.isGuest() ? HomeLoginPromptResponse.guest() : null
        );
    }

    private FortuneReport getMemberLatestReport(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        if (!member.isActive()) {
            throw new CustomException(MemberErrorCode.MEMBER_INACTIVE);
        }

        return fortuneReportRepository.findFirstByMemberIdOrderByCreatedAtDescIdDesc(memberId)
                .orElseThrow(() -> new CustomException(HomeErrorCode.FORTUNE_REPORT_NOT_FOUND));
    }

    private FortuneReport getGuestLatestReport(Long guestSessionId) {
        return fortuneReportRepository.findFirstByGuestSessionIdOrderByCreatedAtDescIdDesc(guestSessionId)
                .orElseThrow(() -> new CustomException(HomeErrorCode.FORTUNE_REPORT_NOT_FOUND));
    }

    private void validateCompleted(FortuneReport report) {
        if (report.getStatus() == FortuneReportStatus.PROCESSING) {
            throw new CustomException(HomeErrorCode.FORTUNE_REPORT_PROCESSING);
        }
        if (report.getStatus() == FortuneReportStatus.FAILED) {
            throw new CustomException(HomeErrorCode.FORTUNE_REPORT_NOT_FOUND);
        }
        if (report.getStatus() != FortuneReportStatus.COMPLETED || !StringUtils.hasText(report.getReportContent())) {
            throw new IllegalStateException("Completed fortune report content is unavailable.");
        }
    }

    private List<RankedPlaceCandidate> rank(List<Place> places, RecommendationScoringContext scoringContext) {
        List<RankedPlaceCandidate> sorted = places.stream()
                .map(place -> {
                    RecommendationMatchContext matchContext = recommendationMatchingService.score(scoringContext, place);
                    return new RankedPlaceCandidate(place, matchContext, matchContext.totalScore(), 0);
                })
                .sorted(Comparator
                        .comparingInt(RankedPlaceCandidate::matchPercentage).reversed()
                        .thenComparing(
                                Comparator.comparing(
                                        (RankedPlaceCandidate candidate) -> candidate.place().getAverageRating(),
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                        )
                        .thenComparing(candidate -> candidate.place().getId()))
                .toList();

        return IntStream.range(0, sorted.size())
                .mapToObj(index -> sorted.get(index).withRankOrder(index + 1))
                .toList();
    }

    private HomeRecommendedPlaceItemResponse toItemResponse(
            RankedPlaceCandidate candidate,
            HomeRecommendedPlaceQuery query,
            String contextPathUrl
    ) {
        Place place = candidate.place();
        PlaceRecommendationSnapshot snapshot = snapshotService.getOrCreate(candidate.matchContext(), place);

        return new HomeRecommendedPlaceItemResponse(
                place.getId(),
                candidate.rankOrder(),
                place.getName(),
                thumbnailUrlFactory.create(place, contextPathUrl),
                place.getElementType(),
                candidate.matchPercentage(),
                snapshot.getWhyItMatches(),
                distanceCalculator.distanceKm(query.latitude(), query.longitude(), place),
                place.getAverageRating()
        );
    }

    private record RankedPlaceCandidate(
            Place place,
            RecommendationMatchContext matchContext,
            int matchPercentage,
            int rankOrder
    ) {
        private RankedPlaceCandidate withRankOrder(int rankOrder) {
            return new RankedPlaceCandidate(place, matchContext, matchPercentage, rankOrder);
        }
    }
}
