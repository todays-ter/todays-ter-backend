package com.umc.todayter.domain.place.dto.request;

import jakarta.validation.constraints.NotNull;

public record PlaceBookmarkRequest(
        @NotNull(message = "저장 여부는 필수입니다.")
        Boolean isSaved
) {
}
