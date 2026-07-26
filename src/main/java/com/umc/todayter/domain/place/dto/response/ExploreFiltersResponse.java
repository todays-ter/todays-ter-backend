package com.umc.todayter.domain.place.dto.response;

import java.util.List;

public record ExploreFiltersResponse(
        List<RegionFilterResponse> regions,
        List<ThemeFilterResponse> themes,
        List<ElementFilterResponse> elements
) {
}
