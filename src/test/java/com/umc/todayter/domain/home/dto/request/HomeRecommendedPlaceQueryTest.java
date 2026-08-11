package com.umc.todayter.domain.home.dto.request;

import com.umc.todayter.domain.home.exception.HomeErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HomeRecommendedPlaceQueryTest {

    @Test
    void coordinatesMayBothBeMissingOrValidBoundaryValues() {
        assertThat(HomeRecommendedPlaceQuery.of(null, null).hasCoordinates()).isFalse();
        assertThat(HomeRecommendedPlaceQuery.of(-90.0, -180.0).hasCoordinates()).isTrue();
        assertThat(HomeRecommendedPlaceQuery.of(90.0, 180.0).hasCoordinates()).isTrue();
        assertThat(HomeRecommendedPlaceQuery.of(37.5665, 126.9780).hasCoordinates()).isTrue();
    }

    @Test
    void invalidCoordinatesThrowHome400_1() {
        assertInvalid(37.0, null);
        assertInvalid(null, 127.0);
        assertInvalid(-90.1, 127.0);
        assertInvalid(90.1, 127.0);
        assertInvalid(37.0, -180.1);
        assertInvalid(37.0, 180.1);
        assertInvalid(Double.NaN, 127.0);
        assertInvalid(37.0, Double.NaN);
        assertInvalid(Double.POSITIVE_INFINITY, 127.0);
        assertInvalid(37.0, Double.NEGATIVE_INFINITY);
    }

    private void assertInvalid(Double latitude, Double longitude) {
        assertThatThrownBy(() -> HomeRecommendedPlaceQuery.of(latitude, longitude))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(HomeErrorCode.INVALID_COORDINATES));
    }
}
