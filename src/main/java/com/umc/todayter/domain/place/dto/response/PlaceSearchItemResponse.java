package com.umc.todayter.domain.place.dto.response;

public record PlaceSearchItemResponse(
        Long placeId,
        String placeName,
        String thumbnailUrl,
        String summary,
        PlaceSearchTypeResponse element,
        PlaceSearchTypeResponse theme,
        Double averageRating,
        Double distanceKm
) {
}
