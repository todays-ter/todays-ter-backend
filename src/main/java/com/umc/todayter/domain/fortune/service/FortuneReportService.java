package com.umc.todayter.domain.fortune.service;

import com.umc.todayter.domain.fortune.config.FortuneReportProperties;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportCreateResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportStatusResponse;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.event.FortuneReportGenerationRequestedEvent;
import com.umc.todayter.domain.fortune.exception.code.FortuneReportErrorCode;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.member.service.MemberService;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FortuneReportService {

    private final FortuneReportRepository fortuneReportRepository;
    private final OnboardingRepository onboardingRepository;
    private final MemberService memberService;
    private final FortuneReportProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FortuneReportCreateResponse create(Long memberId) {
        memberService.getActiveMember(memberId);

        fortuneReportRepository
                .findFirstByMemberIdAndStatusOrderByIdDesc(memberId, FortuneReportStatus.PROCESSING)
                .ifPresent(report -> {
                    throw new CustomException(FortuneReportErrorCode.REPORT_ALREADY_PROCESSING);
                });

        Onboarding onboarding = onboardingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.ONBOARDING_NOT_FOUND));

        if (!onboarding.hasSajuInformation()) {
            throw new CustomException(FortuneReportErrorCode.SAJU_INFORMATION_NOT_FOUND);
        }

        FortuneReport report = fortuneReportRepository.save(FortuneReport.create(memberId, onboarding));
        eventPublisher.publishEvent(new FortuneReportGenerationRequestedEvent(report.getId()));

        return FortuneReportCreateResponse.from(report);
    }

    public FortuneReportStatusResponse getStatus(Long memberId, Long reportId) {
        FortuneReport report = getOwnedReport(memberId, reportId);
        return FortuneReportStatusResponse.from(report, properties.maxRetries());
    }

    @Transactional
    public FortuneReportStatusResponse retry(Long memberId, Long reportId) {
        memberService.getActiveMember(memberId);

        FortuneReport report = fortuneReportRepository.findOwnedByIdForUpdate(reportId, memberId)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() != FortuneReportStatus.FAILED) {
            throw new CustomException(FortuneReportErrorCode.REPORT_NOT_RETRYABLE);
        }
        if (report.getRetryCount() >= properties.maxRetries()) {
            throw new CustomException(FortuneReportErrorCode.RETRY_LIMIT_EXCEEDED);
        }

        report.prepareRetry();
        eventPublisher.publishEvent(new FortuneReportGenerationRequestedEvent(report.getId()));

        return FortuneReportStatusResponse.from(report, properties.maxRetries());
    }

    private FortuneReport getOwnedReport(Long memberId, Long reportId) {
        return fortuneReportRepository.findByIdAndMemberId(reportId, memberId)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
    }
}
