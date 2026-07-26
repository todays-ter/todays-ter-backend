package com.umc.todayter.domain.record.dto.request;

import com.umc.todayter.domain.record.enums.RecordType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecordCreateRequest(
        @NotNull(message = "장소 ID는 필수입니다.")
        Long placeId,

        @NotNull(message = "기록 유형은 필수입니다.")
        RecordType type,

        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1~5 사이여야 합니다.")
        @Max(value = 5, message = "평점은 1~5 사이여야 합니다.")
        Integer rating,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        List<Long> imageIds
) {
}
