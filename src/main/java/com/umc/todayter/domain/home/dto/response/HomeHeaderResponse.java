package com.umc.todayter.domain.home.dto.response;

import com.umc.todayter.global.security.context.CurrentUserType;

import java.time.DayOfWeek;
import java.time.LocalDate;

public record HomeHeaderResponse(
        CurrentUserType userType,
        LocalDate date,
        DayOfWeek dayOfWeek,
        String nickname,
        String greeting,
        String subGreeting
) {
}
