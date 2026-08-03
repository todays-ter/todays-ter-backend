package com.umc.todayter.domain.fortune.dto.response;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;

public record FortuneReportSummaryResponse(
        Long reportId,
        BasicReport basic
) {
}
