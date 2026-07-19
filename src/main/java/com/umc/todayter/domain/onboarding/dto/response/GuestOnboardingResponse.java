package com.umc.todayter.domain.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.CalendarType;
import com.umc.todayter.domain.onboarding.enums.ConcernType;
import com.umc.todayter.domain.onboarding.enums.OnboardingStep;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record GuestOnboardingResponse(
        CalendarType calendarType,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @JsonFormat(pattern = "HH:mm")
        LocalTime birthTime,

        boolean birthTimeUnknown,
        List<ConcernType> concernTypes,
        OnboardingStep onboardingStep
) {
    public static GuestOnboardingResponse from(Onboarding onboarding) {
        List<ConcernType> concernTypes = onboarding.getConcernTypes() == null ? List.of() : List.copyOf(onboarding.getConcernTypes());

        return new GuestOnboardingResponse(
                onboarding.getCalendarType(),
                onboarding.getBirthDate(),
                onboarding.getBirthTime(),
                onboarding.isBirthTimeUnknown(),
                concernTypes,
                onboarding.getOnboardingStep()
        );
    }
}
