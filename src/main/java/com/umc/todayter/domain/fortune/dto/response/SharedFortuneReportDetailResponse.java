package com.umc.todayter.domain.fortune.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ComplementActionGuide;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.DetailSection;
import com.umc.todayter.domain.fortune.enums.FortuneReportCategory;

public record SharedFortuneReportDetailResponse(
        String sharerNickname,
        Long reportId,
        FortuneReportCategory category,
        DetailSection detail,
        @JsonInclude(JsonInclude.Include.NON_NULL) ComplementActionGuide complementActionGuide
) {
    public static SharedFortuneReportDetailResponse from(
            FortuneReportDetailResponse detail,
            String sharerNickname
    ) {
        return new SharedFortuneReportDetailResponse(
                sharerNickname, detail.reportId(), detail.category(),
                detail.detail(), detail.complementActionGuide()
        );
    }
}
