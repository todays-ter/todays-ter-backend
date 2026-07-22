package com.umc.todayter.domain.onboarding.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.todayter.domain.onboarding.enums.CalendarType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.time.LocalTime;

public record GuestSajuRequest(
        @NotNull(message = "달력 유형은 필수입니다.")
        CalendarType calendarType,

        @NotNull(message = "생년월일은 필수입니다.")
        @PastOrPresent(message = "미래의 날짜는 선택할 수 없습니다.")
        LocalDate birthDate,

        LocalTime birthTime,

        @NotNull(message = "출생시간 모름 여부는 필수입니다.")
        Boolean birthTimeUnknown
) {
    @AssertTrue(message = "출생시간 입력 상태가 올바르지 않습니다.")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isBirthTimeValid() {
        if (birthTimeUnknown == null) {
            return true;
        }

        if (birthTimeUnknown) {
            return birthTime == null;
        }

        return birthTime != null;
    }
}
