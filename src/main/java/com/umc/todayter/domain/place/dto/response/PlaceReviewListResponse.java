package com.umc.todayter.domain.place.dto.response;

import java.util.List;

public record PlaceReviewListResponse(
        long totalCount,
        PlaceReviewItemResponse myReview,
        List<PlaceReviewItemResponse> reviews
) {
}
