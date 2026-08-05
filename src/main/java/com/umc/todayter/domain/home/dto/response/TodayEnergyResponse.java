package com.umc.todayter.domain.home.dto.response;

import java.time.LocalDate;

public record TodayEnergyResponse(
        LocalDate date,
        TodayEnergyElementResponse element,
        String description
) {
}
