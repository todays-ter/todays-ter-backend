package com.umc.todayter.domain.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;

public record RecordCreateRequest(
        @NotNull(message = "장소 ID는 필수입니다.")
        Long placeId,

        @NotBlank(message = "기록 내용은 필수입니다.")
        String content,

        @NotNull(message = "방문 날짜는 필수입니다.")
        @PastOrPresent(message = "미래의 날짜는 선택할 수 없습니다.")
        LocalDate visitedAt,

        List<String> imageUrls
) {
}
