package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.RegionFilterResponse;
import com.umc.todayter.domain.place.dto.response.ThemeFilterResponse;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.ThemePlaceCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private PlaceService placeService;

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
