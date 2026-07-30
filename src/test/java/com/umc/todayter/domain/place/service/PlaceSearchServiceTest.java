package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.request.PlaceSearchRequest;
import com.umc.todayter.domain.place.dto.response.PlaceSearchResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceSearchServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void searchPlaces_returnsAppliedFiltersAndPlaceCards() {
        PlaceSearchService placeSearchService = new PlaceSearchService(placeRepository);
        PlaceSearchRequest request = request();
        request.setKeyword(" palace ");
        request.setRegionCode(RegionCode.SEOUL);
        request.setThemeType(ThemeType.WEALTH);
        request.setElementType(ElementType.EARTH);
        Place place = place(1L, "Gyeongbokgung", ElementType.EARTH, ThemeType.WEALTH, "google-place-id");

        when(placeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(place), PageRequest.of(0, 20), 1));

        PlaceSearchResponse response = placeSearchService.searchPlaces(request, "http://localhost:8080");

        assertThat(response.appliedFilters().keyword()).isEqualTo("palace");
        assertThat(response.appliedFilters().regionCode()).isEqualTo("SEOUL");
        assertThat(response.appliedFilters().themeType()).isEqualTo("WEALTH");
        assertThat(response.appliedFilters().elementType()).isEqualTo("EARTH");
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).placeId()).isEqualTo(1L);
        assertThat(response.content().get(0).placeName()).isEqualTo("Gyeongbokgung");
        assertThat(response.content().get(0).summary()).isEqualTo("summary");
        assertThat(response.content().get(0).element().code()).isEqualTo("EARTH");
        assertThat(response.content().get(0).element().name()).isEqualTo(ElementType.EARTH.getDisplayName());
        assertThat(response.content().get(0).theme().code()).isEqualTo("WEALTH");
        assertThat(response.content().get(0).theme().name()).isEqualTo(ThemeType.WEALTH.getDisplayName());
        assertThat(response.content().get(0).averageRating()).isEqualTo(4.7);
        assertThat(response.content().get(0).thumbnailUrl()).isEqualTo("http://localhost:8080/places/1/thumbnail");
        assertThat(response.content().get(0).distanceKm()).isNull();
        assertThat(response.page().number()).isZero();
        assertThat(response.page().size()).isEqualTo(20);
        assertThat(response.page().totalElements()).isEqualTo(1);
        assertThat(response.page().totalPages()).isEqualTo(1);
        assertThat(response.page().hasNext()).isFalse();
    }

    @Test
    void searchPlaces_usesFixedPageSort() {
        PlaceSearchService placeSearchService = new PlaceSearchService(placeRepository);
        PlaceSearchRequest request = request();
        request.setPage(1);
        request.setSize(10);
        when(placeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 0));

        placeSearchService.searchPlaces(request, "http://localhost:8080");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(placeRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("averageRating").getDirection().isDescending()).isTrue();
        assertThat(pageable.getSort().getOrderFor("id").getDirection().isAscending()).isTrue();
    }

    @Test
    void searchPlaces_returnsNullThumbnailWhenGooglePlaceIdIsNullOrBlank() {
        PlaceSearchService placeSearchService = new PlaceSearchService(placeRepository);
        Place nullGooglePlace = place(1L, "null-google", ElementType.FIRE, ThemeType.LOVE, null);
        Place blankGooglePlace = place(2L, "blank-google", ElementType.WOOD, ThemeType.HEALTH, " ");
        when(placeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(nullGooglePlace, blankGooglePlace)));

        PlaceSearchResponse response = placeSearchService.searchPlaces(request(), "http://localhost:8080");

        assertThat(response.content())
                .extracting("thumbnailUrl")
                .containsExactly(null, null);
    }

    @Test
    void searchPlaces_calculatesRoundedDistanceWhenCoordinatesExist() {
        PlaceSearchService placeSearchService = new PlaceSearchService(placeRepository);
        Place place = place(1L, "place", ElementType.FIRE, ThemeType.LOVE, "google-place-id");
        PlaceSearchRequest request = request();
        request.setLatitude(37.5665);
        request.setLongitude(126.9780);
        when(placeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(place)));

        PlaceSearchResponse response = placeSearchService.searchPlaces(request, "http://localhost:8080");

        assertThat(response.content().get(0).distanceKm()).isEqualTo(1.5);
    }

    @Test
    void searchPlaces_returnsEmptyPage() {
        PlaceSearchService placeSearchService = new PlaceSearchService(placeRepository);
        when(placeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        PlaceSearchResponse response = placeSearchService.searchPlaces(request(), "http://localhost:8080");

        assertThat(response.content()).isEmpty();
        assertThat(response.page().totalElements()).isZero();
        assertThat(response.page().totalPages()).isZero();
        assertThat(response.page().hasNext()).isFalse();
    }

    private PlaceSearchRequest request() {
        return new PlaceSearchRequest();
    }

    private Place place(Long id, String name, ElementType elementType, ThemeType themeType, String googlePlaceId) {
        Place place = Place.builder()
                .name(name)
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5796)
                .longitude(126.9770)
                .elementType(elementType)
                .themeType(themeType)
                .averageRating(4.7)
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
