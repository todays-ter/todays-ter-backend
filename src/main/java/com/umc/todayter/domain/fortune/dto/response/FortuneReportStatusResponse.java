package com.umc.todayter.domain.fortune.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.enums.FortuneReportStep;

public record FortuneReportStatusResponse(
        Long reportId,
        FortuneReportStatus status,
        int progress,
        boolean canRetry,
        int retryCount,
        @JsonInclude(JsonInclude.Include.NON_NULL) String failureMessage
) {
    public static FortuneReportStatusResponse from(FortuneReport report, int maxRetries) {
        boolean canRetry = report.getStatus() == FortuneReportStatus.FAILED
                && report.getRetryCount() < maxRetries;
        return new FortuneReportStatusResponse(
                report.getId(),
                report.getStatus(),
                report.getProgress(),
                canRetry,
                report.getRetryCount(),
                report.getStatus() == FortuneReportStatus.FAILED ? report.getFailureMessage() : null
        );
    }
}
