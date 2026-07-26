package com.umc.todayter.domain.fortune.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.todayter.domain.fortune.dto.internal.FortuneReportGenerationContext;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStep;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FortuneReportProgressService {

    private final FortuneReportRepository fortuneReportRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FortuneReportGenerationContext start(Long reportId) {
        FortuneReport report = getReport(reportId);
        report.startAttempt();
        report.advance(FortuneReportStep.BIRTH_DATA_PREPARED, 20);
        return toContext(report);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveManseData(Long reportId, JsonNode manseData) {
        getReport(reportId).saveManseData(manseData);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPromptPrepared(Long reportId) {
        getReport(reportId).advance(FortuneReportStep.PROMPT_PREPARED, 60);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAiReport(Long reportId, String reportContent) {
        getReport(reportId).saveAiReport(reportContent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long reportId) {
        getReport(reportId).complete();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long reportId, String failureCode, String failureMessage) {
        getReport(reportId).fail(failureCode, failureMessage);
    }

    private FortuneReport getReport(Long reportId) {
        return fortuneReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("리포트를 찾을 수 없습니다: " + reportId));
    }

    private FortuneReportGenerationContext toContext(FortuneReport report) {
        return new FortuneReportGenerationContext(
                report.getId(),
                report.getMemberId(),
                report.getCalendarType(),
                report.getBirthDate(),
                report.getBirthTime(),
                report.isBirthTimeUnknown(),
                java.util.List.copyOf(report.getConcernTypes())
        );
    }
}
