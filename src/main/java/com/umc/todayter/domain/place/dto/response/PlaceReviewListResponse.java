package com.umc.todayter.domain.place.dto.response;

import java.util.List;

public record PlaceReviewListResponse(
        List<PlaceReviewItemResponse> content,
        PlaceSearchPageResponse page
) {
}
