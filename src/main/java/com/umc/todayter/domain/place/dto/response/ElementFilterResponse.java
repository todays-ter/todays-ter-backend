package com.umc.todayter.domain.place.dto.response;

public record ElementFilterResponse(
        String code,
        String name,
        int displayOrder
) {
}
