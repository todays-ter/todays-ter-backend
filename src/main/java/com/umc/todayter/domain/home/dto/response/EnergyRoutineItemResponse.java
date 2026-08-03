package com.umc.todayter.domain.home.dto.response;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ActionItem;

public record EnergyRoutineItemResponse(
        int order,
        String type,
        String text
) {

    public static EnergyRoutineItemResponse from(ActionItem action) {
        return new EnergyRoutineItemResponse(
                action.order(),
                action.type(),
                action.text()
        );
    }
}
