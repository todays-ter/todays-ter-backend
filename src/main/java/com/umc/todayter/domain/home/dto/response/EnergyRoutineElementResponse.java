package com.umc.todayter.domain.home.dto.response;

import com.umc.todayter.domain.fortune.enums.FiveElement;

public record EnergyRoutineElementResponse(
        FiveElement code,
        String name
) {

    public static EnergyRoutineElementResponse from(FiveElement element) {
        return new EnergyRoutineElementResponse(
                element,
                element.getLabel()
        );
    }
}
