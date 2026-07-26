package com.umc.todayter.domain.fortune.dto.internal;

import com.umc.todayter.domain.onboarding.enums.CalendarType;
import com.umc.todayter.domain.onboarding.enums.ConcernType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record FortuneReportGenerationContext(
        Long reportId,
        Long memberId,
        CalendarType calendarType,
        LocalDate birthDate,
        LocalTime birthTime,
        boolean birthTimeUnknown,
        List<ConcernType> concernTypes
) {
}
