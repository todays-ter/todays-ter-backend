package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.RegionFilterResponse;
import com.umc.todayter.domain.place.dto.response.ThemeFilterResponse;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.ThemePlaceCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final PlaceRepository placeRepository;

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
