package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListResponse;
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
import com.umc.todayter.domain.record.enums.RecordType;
import com.umc.todayter.domain.record.repository.VisitRecordRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.AuthPrincipal;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserContextResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private SavedPlaceRepository savedPlaceRepository;

    @Mock
    private VisitRecordRepository visitRecordRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PlaceRecommendationSnapshotService recommendationSnapshotService;

    @Mock
    private CurrentUserContextResolver currentUserContextResolver;

    @InjectMocks
    private PlaceService placeService;

    @BeforeEach
    void setUpAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthPrincipal(1L), null, Collections.emptyList())
        );
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getExploreFilters_countsActivePlacesByThemeAndFillsMissingThemesWithZero() {
        when(placeRepository.countActivePlacesGroupByThemeType())
                .thenReturn(List.of(
                        themePlaceCount(ThemeType.LOVE, 2L),
                        themePlaceCount(ThemeType.CAREER, 1L)
                ));

        ExploreFiltersResponse response = placeService.getExploreFilters();

        assertThat(response.themes())
                .extracting(ThemeFilterResponse::code, ThemeFilterResponse::placeCount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("LOVE", 2L),
                        org.assertj.core.groups.Tuple.tuple("CAREER", 1L),
                        org.assertj.core.groups.Tuple.tuple("WEALTH", 0L),
                        org.assertj.core.groups.Tuple.tuple("RELATIONSHIP", 0L),
                        org.assertj.core.groups.Tuple.tuple("HEALTH", 0L),
                        org.assertj.core.groups.Tuple.tuple("ETC", 0L)
                );
        verify(placeRepository).countActivePlacesGroupByThemeType();
    }

    @Test
    void updateBookmark_trueSavesPlaceWhenNotAlreadySaved() {
        Member member = member(1L);
        Place place = place(10, 20, 30, 40, 50, 60);
        when(memberRepository.findByIdAndStatusForUpdate(1L, MemberStatus.ACTIVE)).thenReturn(Optional.of(member));
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(savedPlaceRepository.findByMemberIdAndPlaceId(1L, 1L)).thenReturn(Optional.empty());
        when(savedPlaceRepository.save(any(SavedPlace.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlaceBookmarkResponse response = placeService.updateBookmark(1L, true);

        assertThat(response.placeId()).isEqualTo(1L);
        assertThat(response.isSaved()).isTrue();
        verify(savedPlaceRepository).save(any(SavedPlace.class));
    }

    @Test
    void updateBookmark_falseDeletesSavedPlaceIdempotently() {
        Member member = member(1L);
        Place place = place(10, 20, 30, 40, 50, 60);
        when(memberRepository.findByIdAndStatusForUpdate(1L, MemberStatus.ACTIVE)).thenReturn(Optional.of(member));
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));

        PlaceBookmarkResponse response = placeService.updateBookmark(1L, false);

        assertThat(response.isSaved()).isFalse();
        verify(savedPlaceRepository).deleteByMemberIdAndPlaceId(1L, 1L);
    }

    @Test
    void getRecommendedPlaceDetailReturnsElementCategoriesAndMatchingScore() {
        Place place = place(20, 22, 18, 28, 30, 28);
        CurrentUserContext userContext = CurrentUserContext.forMember(1L);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(savedPlaceRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(true);
        when(currentUserContextResolver.resolve(null)).thenReturn(userContext);
        PlaceRecommendationSnapshot snapshot = PlaceRecommendationSnapshot.create(
                10L,
                1L,
                java.time.LocalDate.of(2026, 8, 3),
                "HEALTH",
                87,
                List.of("주 오행 수", "오늘 흐름 상생", "휴식·회복"),
                "이 장소가 오늘의 흐름과 잘 맞아요.",
                "물길을 따라 가볍게 걸어보세요."
        );
        when(recommendationSnapshotService.getOrCreate(userContext, place)).thenReturn(Optional.of(snapshot));

        var response = placeService.getRecommendedPlaceDetail(1L, "https://api.example.com", null);

        assertThat(response.primaryElement().code()).isEqualTo(place.getElementType().name());
        assertThat(response.primaryElement().name()).isEqualTo(place.getElementType().getDisplayName());
        assertThat(response.topCategories()).containsExactly("휴식·회복");
        assertThat(response.matchingScore()).isEqualTo(87);
        assertThat(response.whyItMatches()).isEqualTo("이 장소가 오늘의 흐름과 잘 맞아요.");
        assertThat(response.actionSuggestion()).isEqualTo("물길을 따라 가볍게 걸어보세요.");
        assertThat(response.isSaved()).isTrue();
    }

    @Test
    void snapshotWritingMethodsUseWritableTransactions() throws NoSuchMethodException {
        Transactional detailTransaction = PlaceService.class.getDeclaredMethod(
                "getRecommendedPlaceDetail", Long.class, String.class, String.class
        ).getAnnotation(Transactional.class);
        Transactional shareTransaction = PlaceService.class.getDeclaredMethod(
                "createRecommendationShareToken", Long.class, String.class
        ).getAnnotation(Transactional.class);

        assertThat(detailTransaction).isNotNull();
        assertThat(detailTransaction.readOnly()).isFalse();
        assertThat(shareTransaction).isNotNull();
        assertThat(shareTransaction.readOnly()).isFalse();
    }

    @Test
    void getExploreFilters_returnsRegionsThemesAndElementsInDisplayOrder() {
        when(placeRepository.countActivePlacesGroupByThemeType()).thenReturn(List.of());

        ExploreFiltersResponse response = placeService.getExploreFilters();

        assertThat(response.regions())
                .extracting(RegionFilterResponse::code)
                .containsExactly("ALL", "SEOUL", "JEJU", "BUSAN", "GANGWON", "CAPITAL_AREA");
        assertThat(response.regions())
                .extracting(RegionFilterResponse::displayOrder)
                .containsExactly(0, 1, 2, 3, 4, 5);

        assertThat(response.themes())
                .extracting(ThemeFilterResponse::code)
                .containsExactly("LOVE", "CAREER", "WEALTH", "RELATIONSHIP", "HEALTH", "ETC");
        assertThat(response.themes())
                .extracting(ThemeFilterResponse::displayOrder)
                .containsExactly(1, 2, 3, 4, 5, 6);

        assertThat(response.elements())
                .extracting(ElementFilterResponse::code)
                .containsExactly("ALL", "FIRE", "EARTH", "WOOD", "WATER", "METAL");
        assertThat(response.elements())
                .extracting(ElementFilterResponse::displayOrder)
                .containsExactly(0, 1, 2, 3, 4, 5);
    }

    @Test
    void getExploreFilters_includesAllOnlyInRegionsAndElements() {
        when(placeRepository.countActivePlacesGroupByThemeType()).thenReturn(List.of());

        ExploreFiltersResponse response = placeService.getExploreFilters();

        assertThat(response.regions().get(0).code()).isEqualTo("ALL");
        assertThat(response.elements().get(0).code()).isEqualTo("ALL");
        assertThat(response.themes())
                .extracting(ThemeFilterResponse::code)
                .doesNotContain("ALL");
    }

    @Test
    void getMyPlaces_savedType_returnsSavedPlacesOrderedByCreatedAtDesc() {
        Member member = member(1L);
        Place place = place(10, 50, 30, 5, 20, 1);
        SavedPlace savedPlace = savedPlace(member, place);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(savedPlaceRepository.findAllByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(savedPlace));

        PlaceListResponse response = placeService.getMyPlaces("saved");

        assertThat(response.places()).hasSize(1);
        assertThat(response.places().get(0).placeId()).isEqualTo(place.getId());
        assertThat(response.places().get(0).categories()).containsExactly("관계", "일·커리어");
        assertThat(response.places().get(0).element()).isEqualTo("화");
        assertThat(response.places().get(0).thumbnailUrl()).isEqualTo("/places/%d/thumbnail".formatted(place.getId()));
        verify(savedPlaceRepository).findAllByMemberIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getMyPlaces_categoriesReturnsOnlyOneWhenSecondPlaceIsTied() {
        Member member = member(1L);
        // love=50(1등 확정), relationship=30, career=30(2등 동점) -> 2등을 특정할 수 없어 1개만 반환
        Place place = place(50, 30, 30, 5, 20, 1);
        SavedPlace savedPlace = savedPlace(member, place);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(savedPlaceRepository.findAllByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(savedPlace));

        PlaceListResponse response = placeService.getMyPlaces("saved");

        assertThat(response.places().get(0).categories()).containsExactly("연애");
    }

    @Test
    void getMyPlaces_visitedType_returnsLatestVisitRecordPerPlace() {
        Member member = member(1L);
        Place place = place(10, 50, 30, 5, 20, 1);
        VisitRecord visitRecord = visitRecord(member, place);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(visitRecordRepository.findLatestPerPlaceByMemberId(1L)).thenReturn(List.of(visitRecord));

        PlaceListResponse response = placeService.getMyPlaces("visited");

        assertThat(response.places()).hasSize(1);
        assertThat(response.places().get(0).placeId()).isEqualTo(place.getId());
        verify(visitRecordRepository).findLatestPerPlaceByMemberId(1L);
    }

    @Test
    void getMyPlaces_missingType_throwsMissingTypeParameter() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L)));

        assertThatThrownBy(() -> placeService.getMyPlaces(null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.MISSING_TYPE_PARAMETER);
    }

    @Test
    void getMyPlaces_invalidType_throwsInvalidTypeParameter() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L)));

        assertThatThrownBy(() -> placeService.getMyPlaces("bookmarked"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.INVALID_TYPE_PARAMETER);
    }

    @Test
    void getMyPlaces_memberNotFound_throwsMemberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.getMyPlaces("saved"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    private Member member(Long id) {
        Member member = Member.create("nickname" + id);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private SavedPlace savedPlace(Member member, Place place) {
        SavedPlace savedPlace = SavedPlace.builder()
                .member(member)
                .place(place)
                .build();
        ReflectionTestUtils.setField(savedPlace, "createdAt", LocalDateTime.of(2026, 6, 29, 10, 0));
        return savedPlace;
    }

    private VisitRecord visitRecord(Member member, Place place) {
        VisitRecord visitRecord = VisitRecord.create(member, place, RecordType.RECORD, 4, "좋았어요");
        ReflectionTestUtils.setField(visitRecord, "createdAt", LocalDateTime.of(2026, 6, 28, 10, 0));
        return visitRecord;
    }

    private Place place(int loveScore, int relationshipScore, int careerScore, int studyScore, int restScore, int transitionScore) {
        Place place = Place.builder()
                .name("경복궁")
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5796)
                .longitude(126.9770)
                .elementType(ElementType.FIRE)
                .themeType(ThemeType.LOVE)
                .averageRating(0.0)
                .reviewCount(0)
                .editorPick(false)
                .active(true)
                .terrainType("궁궐")
                .loveScore(loveScore)
                .relationshipScore(relationshipScore)
                .careerScore(careerScore)
                .studyScore(studyScore)
                .restScore(restScore)
                .transitionScore(transitionScore)
                .build();
        ReflectionTestUtils.setField(place, "id", 1L);
        return place;
    }

    private ThemePlaceCount themePlaceCount(ThemeType themeType, long placeCount) {
        return new ThemePlaceCount() {
            @Override
            public ThemeType getThemeType() {
                return themeType;
            }

            @Override
            public long getPlaceCount() {
                return placeCount;
            }
        };
    }
}
