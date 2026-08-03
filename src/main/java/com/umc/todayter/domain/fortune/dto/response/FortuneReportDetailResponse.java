package com.umc.todayter.domain.fortune.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ComplementActionGuide;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.DetailSection;
import com.umc.todayter.domain.fortune.enums.FortuneReportCategory;

public record FortuneReportDetailResponse(
        Long reportId,
        FortuneReportCategory category,
        DetailSection detail,
        @JsonInclude(JsonInclude.Include.NON_NULL) ComplementActionGuide complementActionGuide
) {
}
