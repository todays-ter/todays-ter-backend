package com.umc.todayter.domain.place.dto.response;

import org.springframework.data.domain.Page;

public record PlaceSearchPageResponse(
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static PlaceSearchPageResponse from(Page<?> page) {
        return new PlaceSearchPageResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
