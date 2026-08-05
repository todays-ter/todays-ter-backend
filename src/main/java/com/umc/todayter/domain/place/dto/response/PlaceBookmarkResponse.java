package com.umc.todayter.domain.place.dto.response;

public record PlaceBookmarkResponse(
        Long placeId,
        boolean isSaved
) {
}
