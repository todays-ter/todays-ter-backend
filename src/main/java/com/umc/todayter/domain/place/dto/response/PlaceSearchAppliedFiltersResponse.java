package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;

public record PlaceSearchAppliedFiltersResponse(
        String keyword,
        String regionCode,
        String themeType,
        String elementType
) {
    public static PlaceSearchAppliedFiltersResponse of(
            String keyword,
            RegionCode regionCode,
            ThemeType themeType,
            ElementType elementType
    ) {
        return new PlaceSearchAppliedFiltersResponse(
                keyword,
                regionCode == null ? null : regionCode.name(),
                themeType == null ? null : themeType.name(),
                elementType == null ? null : elementType.name()
        );
    }
}
