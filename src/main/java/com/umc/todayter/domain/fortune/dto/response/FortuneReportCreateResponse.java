package com.umc.todayter.domain.fortune.dto.response;

import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;

public record FortuneReportCreateResponse(
        Long reportId,
        FortuneReportStatus status
) {
    public static FortuneReportCreateResponse from(
            FortuneReport report
    ) {
        return new FortuneReportCreateResponse(report.getId(), report.getStatus());
    }
}
