package com.umc.todayter.domain.place.dto.response;

public record RegionFilterResponse(
        String code,
        String name,
        int displayOrder
) {
}
