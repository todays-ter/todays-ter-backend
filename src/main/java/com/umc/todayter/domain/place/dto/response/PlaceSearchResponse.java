package com.umc.todayter.domain.place.dto.response;

import java.util.List;

public record PlaceSearchResponse(
        PlaceSearchAppliedFiltersResponse appliedFilters,
        List<PlaceSearchItemResponse> content,
        PlaceSearchPageResponse page
) {
}
