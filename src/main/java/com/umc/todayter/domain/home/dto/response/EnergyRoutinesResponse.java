package com.umc.todayter.domain.home.dto.response;

import java.util.List;

public record EnergyRoutinesResponse(
        EnergyRoutineElementResponse element,
        List<EnergyRoutineItemResponse> routines
) {

    public EnergyRoutinesResponse {
        routines = List.copyOf(routines);
    }
}
