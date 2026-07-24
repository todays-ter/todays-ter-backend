package com.umc.todayter.domain.place.dto.response;

public record ThemeFilterResponse(
        String code,
        String name,
        long placeCount,
        int displayOrder
) {
}
