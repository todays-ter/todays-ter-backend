package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListResponse;
import com.umc.todayter.domain.place.dto.response.RecommendationPlaceDetailResponse;
import com.umc.todayter.domain.place.dto.response.SharedRecommendationPlaceDetailResponse;
import com.umc.todayter.domain.place.dto.response.PlaceBookmarkResponse;
import com.umc.todayter.domain.place.dto.response.RegionFilterResponse;
import com.umc.todayter.domain.place.dto.response.ThemeFilterResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.SavedPlace;
import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.exception.PlaceErrorCode;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.SavedPlaceRepository;
import com.umc.todayter.domain.place.repository.ThemePlaceCount;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.repository.VisitRecordRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.SecurityUtil;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserContextResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final String ALL_CODE = "ALL";
    private static final String ALL_NAME = "전체";
    private static final int ALL_DISPLAY_ORDER = 0;
    private static final String TYPE_SAVED = "saved";
    private static final String TYPE_VISITED = "visited";

    private final PlaceRepository placeRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final MemberRepository memberRepository;
    private final FortuneReportRepository fortuneReportRepository;
    private final PlaceRecommendationSnapshotService recommendationSnapshotService;
    private final CurrentUserContextResolver currentUserContextResolver;

    public PlaceListResponse getMyPlaces(String type) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (type == null || type.isBlank()) {
            throw new CustomException(PlaceErrorCode.MISSING_TYPE_PARAMETER);
        }

        List<PlaceListItemResponse> places = switch (type) {
            case TYPE_SAVED -> savedPlaceRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream()
                    .map(saved -> PlaceListItemResponse.from(saved.getPlace(), saved.getCreatedAt().toLocalDate()))
                    .toList();
            case TYPE_VISITED -> visitRecordRepository.findLatestPerPlaceByMemberId(memberId).stream()
                    .map((VisitRecord vr) -> PlaceListItemResponse.from(vr.getPlace(), vr.getCreatedAt().toLocalDate()))
                    .toList();
            default -> throw new CustomException(PlaceErrorCode.INVALID_TYPE_PARAMETER);
        };

        return new PlaceListResponse(places);
    }

    @Transactional
    public RecommendationPlaceDetailResponse getRecommendedPlaceDetail(
            Long placeId,
            String contextPathUrl,
            String guestId
    ) {
        Place place = getActivePlace(placeId);
        Long memberId = SecurityUtil.getCurrentMemberIdOrNull();
        CurrentUserContext userContext = resolveOptionalUserContext(memberId, guestId);

        boolean isSaved = memberId != null && savedPlaceRepository.existsByMemberIdAndPlaceId(memberId, placeId);
        PlaceRecommendationSnapshot snapshot = recommendationSnapshotService
                .getOrCreate(userContext, place)
                .orElse(null);

        return RecommendationPlaceDetailResponse.from(place, isSaved, snapshot, contextPathUrl);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String createRecommendationShareToken(Long placeId, String guestId) {
        Place place = getActivePlace(placeId);
        CurrentUserContext userContext = currentUserContextResolver.resolve(guestId);
        PlaceRecommendationSnapshot snapshot = recommendationSnapshotService
                .getOrCreateRequired(userContext, place);
        return recommendationSnapshotService.enableSharing(snapshot).getShareToken();
    }

    public SharedRecommendationPlaceDetailResponse getSharedRecommendedPlaceDetail(
            String shareToken,
            String contextPathUrl
    ) {
        PlaceRecommendationSnapshot snapshot = recommendationSnapshotService.getShared(shareToken);
        Place place = getActivePlace(snapshot.getPlaceId());
        String sharerNickname = fortuneReportRepository.findById(snapshot.getFortuneReportId())
                .filter(report -> report.getMemberId() != null)
                .flatMap(report -> memberRepository.findByIdAndStatus(report.getMemberId(), MemberStatus.ACTIVE))
                .map(Member::getNickname)
                .orElse(null);
        RecommendationPlaceDetailResponse detail = RecommendationPlaceDetailResponse.from(
                place, false, snapshot, contextPathUrl
        );
        return SharedRecommendationPlaceDetailResponse.from(detail, sharerNickname);
    }

    private CurrentUserContext resolveOptionalUserContext(Long memberId, String guestId) {
        if (memberId == null && !StringUtils.hasText(guestId)) {
            return null;
        }
        return currentUserContextResolver.resolve(guestId);
    }

    public void validateActivePlace(Long placeId) {
        getActivePlace(placeId);
    }

    @Transactional
    public PlaceBookmarkResponse updateBookmark(Long placeId, boolean isSaved) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findByIdAndStatusForUpdate(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        Place place = getActivePlace(placeId);

        if (isSaved) {
            savedPlaceRepository.findByMemberIdAndPlaceId(memberId, placeId)
                    .orElseGet(() -> savedPlaceRepository.save(
                            SavedPlace.builder()
                                    .member(member)
                                    .place(place)
                                    .build()
                    ));
        } else {
            savedPlaceRepository.deleteByMemberIdAndPlaceId(memberId, placeId);
        }

        return new PlaceBookmarkResponse(placeId, isSaved);
    }

    private Place getActivePlace(Long placeId) {
        return placeRepository.findByIdAndActiveTrue(placeId)
                .orElseThrow(() -> new CustomException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    public ExploreFiltersResponse getExploreFilters() {
        Map<ThemeType, Long> themeCounts = countActivePlacesByTheme();

        return new ExploreFiltersResponse(
                getRegionFilters(),
                getThemeFilters(themeCounts),
                getElementFilters()
        );
    }

    private Map<ThemeType, Long> countActivePlacesByTheme() {
        Map<ThemeType, Long> themeCounts = new EnumMap<>(ThemeType.class);

        placeRepository.countActivePlacesGroupByThemeType()
                .forEach(count -> themeCounts.put(count.getThemeType(), count.getPlaceCount()));

        return themeCounts;
    }

    private List<RegionFilterResponse> getRegionFilters() {
        List<RegionFilterResponse> regions = Arrays.stream(RegionCode.values())
                .sorted(Comparator.comparingInt(RegionCode::getDisplayOrder))
                .map(region -> new RegionFilterResponse(
                        region.name(),
                        region.getDisplayName(),
                        region.getDisplayOrder()
                ))
                .toList();

        return prependAllRegion(regions);
    }

    private List<RegionFilterResponse> prependAllRegion(List<RegionFilterResponse> regions) {
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(new RegionFilterResponse(ALL_CODE, ALL_NAME, ALL_DISPLAY_ORDER)),
                        regions.stream()
                )
                .toList();
    }

    private List<ThemeFilterResponse> getThemeFilters(Map<ThemeType, Long> themeCounts) {
        return Arrays.stream(ThemeType.values())
                .sorted(Comparator.comparingInt(ThemeType::getDisplayOrder))
                .map(theme -> new ThemeFilterResponse(
                        theme.name(),
                        theme.getDisplayName(),
                        themeCounts.getOrDefault(theme, 0L),
                        theme.getDisplayOrder()
                ))
                .toList();
    }

    private List<ElementFilterResponse> getElementFilters() {
        List<ElementFilterResponse> elements = Arrays.stream(ElementType.values())
                .sorted(Comparator.comparingInt(ElementType::getDisplayOrder))
                .map(element -> new ElementFilterResponse(
                        element.name(),
                        element.getDisplayName(),
                        element.getDisplayOrder()
                ))
                .toList();

        return prependAllElement(elements);
    }

    private List<ElementFilterResponse> prependAllElement(List<ElementFilterResponse> elements) {
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(new ElementFilterResponse(ALL_CODE, ALL_NAME, ALL_DISPLAY_ORDER)),
                        elements.stream()
                )
                .toList();
    }
}
