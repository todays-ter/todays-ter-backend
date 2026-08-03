package com.umc.todayter.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.CalendarType;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberSajuResponse(

        CalendarType calendarType,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @JsonFormat(pattern = "HH:mm")
        LocalTime birthTime,

        boolean birthTimeUnknown
) {
    public static MemberSajuResponse from(Onboarding onboarding) {
        return new MemberSajuResponse(
                onboarding.getCalendarType(),
                onboarding.getBirthDate(),
                onboarding.getBirthTime(),
                onboarding.isBirthTimeUnknown()
        );
    }
}
