package com.umc.todayter.domain.home.dto.response;

import com.umc.todayter.domain.fortune.enums.FiveElement;

public record TodayEnergyElementResponse(
        FiveElement code,
        String name
) {

    public static TodayEnergyElementResponse from(FiveElement element) {
        return new TodayEnergyElementResponse(element, element.getLabel());
    }
}
