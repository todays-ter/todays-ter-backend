package com.umc.todayter.domain.place.client.dto;

import java.util.List;

public record GooglePlaceDetailsResponse(
        List<Photo> photos
) {
    public record Photo(
            String name
    ) {
    }
}
