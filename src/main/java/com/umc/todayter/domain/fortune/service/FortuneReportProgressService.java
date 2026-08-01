package com.umc.todayter.domain.fortune.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import com.umc.todayter.domain.fortune.dto.internal.FortuneReportGenerationContext;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStep;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FortuneReportProgressService {

    private final FortuneReportRepository fortuneReportRepository;
    private final OnboardingRepository onboardingRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FortuneReportGenerationContext start(Long reportId) {
        FortuneReport report = getReport(reportId);
        report.startAttempt();
        report.advance(
                report.getManseData() == null
                        ? FortuneReportStep.BIRTH_DATA_PREPARED
                        : FortuneReportStep.MANSE_DATA_CREATED,
                report.getManseData() == null ? 20 : 40
        );
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
        Onboarding onboarding = onboardingRepository.findById(report.getOnboardingId())
                .orElseThrow(() -> new IllegalStateException(
                        "리포트의 온보딩 정보를 찾을 수 없습니다: " + report.getOnboardingId()
                ));
        return new FortuneReportGenerationContext(
                report.getId(),
                report.getMemberId(),
                onboarding.getGender(),
                onboarding.getCalendarType(),
                onboarding.getBirthDate(),
                onboarding.getBirthTime(),
                onboarding.isBirthTimeUnknown(),
                onboarding.getConcernTypes() == null
                        ? java.util.List.of()
                        : java.util.List.copyOf(onboarding.getConcernTypes()),
                parseManseData(report.getManseData())
        );
    }

    private JsonNode parseManseData(String manseData) {
        if (manseData == null || manseData.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(manseData);
        } catch (JacksonException e) {
            throw new IllegalStateException("저장된 만세력 정보를 읽을 수 없습니다.", e);
        }
    }
}
