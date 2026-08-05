package com.umc.todayter.domain.home.dto.response;

import com.umc.todayter.domain.place.enums.ElementType;

public record HomeRecommendedPlaceItemResponse(
        Long placeId,
        int rankOrder,
        String placeName,
        String thumbnailUrl,
        ElementType placeElement,
        int matchPercentage,
        String recommendationReason,
        Double distanceKm,
        Double averageRating
) {
}
