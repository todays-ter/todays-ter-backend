package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListResponse;
import com.umc.todayter.domain.place.dto.response.RegionFilterResponse;
import com.umc.todayter.domain.place.dto.response.ThemeFilterResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.SavedPlace;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
