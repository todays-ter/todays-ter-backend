package com.umc.todayter.domain.record.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record RecordUpdateRequest(
        @Min(value = 1, message = "평점은 1~5 사이여야 합니다.")
        @Max(value = 5, message = "평점은 1~5 사이여야 합니다.")
        Integer rating,

        String content,

        List<Long> imageIds
) {
}
