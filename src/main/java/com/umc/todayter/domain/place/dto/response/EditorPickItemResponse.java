package com.umc.todayter.domain.place.dto.response;

public record EditorPickItemResponse(
        Long placeId,
        String placeName,
        String thumbnailUrl,
        String summary,
        String description,
        PlaceSearchTypeResponse element,
        PlaceSearchTypeResponse theme,
        Double averageRating
) {
}
