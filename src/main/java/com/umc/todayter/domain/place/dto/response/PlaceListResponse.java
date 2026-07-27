package com.umc.todayter.domain.place.dto.response;

import java.util.List;

public record PlaceListResponse(
        List<PlaceListItemResponse> places
) {
}
