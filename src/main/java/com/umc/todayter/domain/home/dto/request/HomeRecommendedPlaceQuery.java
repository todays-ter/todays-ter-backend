package com.umc.todayter.domain.home.dto.request;

import com.umc.todayter.domain.home.exception.HomeErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;

public record HomeRecommendedPlaceQuery(
        Double latitude,
        Double longitude
) {

    public static HomeRecommendedPlaceQuery of(Double latitude, Double longitude) {
        HomeRecommendedPlaceQuery query = new HomeRecommendedPlaceQuery(latitude, longitude);
        query.validate();
        return query;
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    private void validate() {
        if ((latitude == null) != (longitude == null)) {
            throw new CustomException(HomeErrorCode.INVALID_COORDINATES);
        }
        if (latitude == null) {
            return;
        }
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90.0
                || latitude > 90.0
                || longitude < -180.0
                || longitude > 180.0) {
            throw new CustomException(HomeErrorCode.INVALID_COORDINATES);
        }
    }
}
