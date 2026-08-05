package com.umc.todayter.domain.fortune.service;

import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.fortune.service.provider.ComplementActionProvider;
import com.umc.todayter.global.config.ablecityProperties.FortuneReportProperties;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportCreateResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportStatusResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportDetailResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportSummaryResponse;
import com.umc.todayter.domain.fortune.dto.response.SharedFortuneReportDetailResponse;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.enums.FortuneReportCategory;
import com.umc.todayter.domain.fortune.event.FortuneReportGenerationRequestedEvent;
import com.umc.todayter.domain.fortune.exception.code.FortuneReportErrorCode;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.member.service.MemberService;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.dto.response.ShareLinkResponse;
import com.umc.todayter.global.service.ShareUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FortuneReportService {

    private final FortuneReportRepository fortuneReportRepository;
    private final OnboardingRepository onboardingRepository;
    private final GuestSessionRepository guestSessionRepository;
    private final MemberService memberService;
    private final FortuneReportProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final FortuneReportResultParser resultParser;
    private final ComplementActionProvider complementActionProvider;
    private final ShareUrlService shareUrlService;

    @Transactional
    public FortuneReportCreateResponse create(Long memberId, String guestId) {
        if (memberId != null) {
            return createForMember(memberId);
        }
        return createForGuest(guestId);
    }

    private FortuneReportCreateResponse createForMember(Long memberId) {
        memberService.getActiveMemberForUpdate(memberId);

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

        FortuneReport report = fortuneReportRepository.save(FortuneReport.createForMember(memberId, onboarding));
        generateOrApplyMockResult(report);

        return FortuneReportCreateResponse.from(report);
    }

    private FortuneReportCreateResponse createForGuest(String guestId) {
        GuestSession guestSession = getValidGuestSessionForUpdate(guestId);

        fortuneReportRepository
                .findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                        guestSession.getId(), FortuneReportStatus.PROCESSING
                )
                .ifPresent(report -> {
                    throw new CustomException(FortuneReportErrorCode.REPORT_ALREADY_PROCESSING);
                });

        Onboarding onboarding = onboardingRepository.findByGuestSessionId(guestSession.getId())
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.ONBOARDING_NOT_FOUND));

        if (!onboarding.hasSajuInformation()) {
            throw new CustomException(FortuneReportErrorCode.SAJU_INFORMATION_NOT_FOUND);
        }

        FortuneReport report = fortuneReportRepository.save(
                FortuneReport.createForGuest(guestSession.getId(), onboarding)
        );
        generateOrApplyMockResult(report);

        return FortuneReportCreateResponse.from(report);
    }

    private void generateOrApplyMockResult(FortuneReport report) {
        if (!properties.mockEnabled()) {
            eventPublisher.publishEvent(new FortuneReportGenerationRequestedEvent(report.getId()));
            return;
        }

        Long templateReportId = properties.mockTemplateReportId();
        if (templateReportId == null) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }

        FortuneReport template = fortuneReportRepository.findById(templateReportId)
                .filter(source -> source.getStatus() == FortuneReportStatus.COMPLETED)
                .filter(source -> source.getManseData() != null && !source.getManseData().isBlank())
                .filter(source -> source.getReportContent() != null && !source.getReportContent().isBlank())
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE));

        report.completeWithGeneratedResult(template.getManseData(), template.getReportContent());
    }

    public FortuneReportStatusResponse getStatus(Long memberId, String guestId, Long reportId) {
        FortuneReport report = memberId != null
                ? getMemberOwnedReport(memberId, reportId)
                : getGuestOwnedReport(getValidGuestSession(guestId).getId(), reportId);
        return FortuneReportStatusResponse.from(report, properties.maxRetries());
    }

    public FortuneReportSummaryResponse getSummary(Long memberId, String guestId, Long reportId) {
        FortuneReport report = getCompletedOwnedReport(memberId, guestId, reportId);
        return toSummaryResponse(report);
    }

    private FortuneReportSummaryResponse toSummaryResponse(FortuneReport report) {
        return new FortuneReportSummaryResponse(
                report.getId(),
                resultParser.parseBasic(report.getReportContent())
        );
    }

    public FortuneReportDetailResponse getDetail(
            Long memberId,
            String guestId,
            Long reportId,
            String categoryValue
    ) {
        FortuneReportCategory category = FortuneReportCategory.from(categoryValue);
        if (category == null) {
            throw new CustomException(FortuneReportErrorCode.INVALID_REPORT_CATEGORY);
        }

        FortuneReport report = getCompletedOwnedReport(memberId, guestId, reportId);
        return toDetailResponse(report, category);
    }

    private FortuneReportDetailResponse toDetailResponse(
            FortuneReport report,
            FortuneReportCategory category
    ) {
        FortuneReportResultParser.ParsedReport parsed = resultParser.parse(
                report.getReportContent(), report.getManseData()
        );
        var detail = parsed.details().stream()
                .filter(section -> category.name().equals(section.code()))
                .findFirst()
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE));

        var actionGuide = category == FortuneReportCategory.GENERAL
                ? complementActionProvider.select(parsed.complementElement(), report.getId())
                : null;

        return new FortuneReportDetailResponse(report.getId(), category, detail, actionGuide);
    }

    @Transactional
    public ShareLinkResponse createShareLink(Long memberId, String guestId, Long reportId) {
        FortuneReport report;
        if (memberId != null) {
            memberService.getActiveMember(memberId);
            report = fortuneReportRepository.findOwnedByIdForUpdate(reportId, memberId)
                    .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
        } else {
            Long guestSessionId = getValidGuestSession(guestId).getId();
            report = fortuneReportRepository.findGuestOwnedByIdForUpdate(reportId, guestSessionId)
                    .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
        }

        validateCompletedReport(report);
        if (report.getShareToken() == null) {
            report.enableSharing(createUniqueShareToken());
        }

        return ShareLinkResponse.forFortuneReport(
                report.getShareToken(),
                shareUrlService.fortuneReportUrl(report.getShareToken())
        );
    }

    public SharedFortuneReportDetailResponse getSharedDetail(String shareToken, String categoryValue) {
        FortuneReportCategory category = FortuneReportCategory.from(categoryValue);
        if (category == null) {
            throw new CustomException(FortuneReportErrorCode.INVALID_REPORT_CATEGORY);
        }
        FortuneReport report = getCompletedSharedReport(shareToken);
        FortuneReportDetailResponse response = toDetailResponse(report, category);
        String sharerNickname = report.getMemberId() == null
                ? null
                : memberService.getActiveMember(report.getMemberId()).getNickname();
        return SharedFortuneReportDetailResponse.from(response, sharerNickname);
    }

    private FortuneReport getCompletedSharedReport(String shareToken) {
        if (shareToken == null || shareToken.length() != 32) {
            throw new CustomException(FortuneReportErrorCode.SHARED_REPORT_NOT_FOUND);
        }
        FortuneReport report = fortuneReportRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.SHARED_REPORT_NOT_FOUND));
        validateCompletedReport(report);
        return report;
    }

    private String createUniqueShareToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (fortuneReportRepository.existsByShareToken(token));
        return token;
    }

    private FortuneReport getCompletedOwnedReport(Long memberId, String guestId, Long reportId) {
        FortuneReport report = memberId != null
                ? getMemberOwnedReport(memberId, reportId)
                : getGuestOwnedReport(getValidGuestSession(guestId).getId(), reportId);

        validateCompletedReport(report);
        return report;
    }

    private void validateCompletedReport(FortuneReport report) {
        if (report.getStatus() != FortuneReportStatus.COMPLETED) {
            throw new CustomException(FortuneReportErrorCode.REPORT_NOT_COMPLETED);
        }
        if (report.getReportContent() == null || report.getReportContent().isBlank()) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }
    }

    @Transactional
    public FortuneReportStatusResponse retry(Long memberId, String guestId, Long reportId) {
        FortuneReport report;
        if (memberId != null) {
            memberService.getActiveMember(memberId);
            report = fortuneReportRepository.findOwnedByIdForUpdate(reportId, memberId)
                    .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
        } else {
            Long guestSessionId = getValidGuestSession(guestId).getId();
            report = fortuneReportRepository.findGuestOwnedByIdForUpdate(reportId, guestSessionId)
                    .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
        }

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

    private FortuneReport getMemberOwnedReport(Long memberId, Long reportId) {
        return fortuneReportRepository.findByIdAndMemberId(reportId, memberId)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
    }

    private FortuneReport getGuestOwnedReport(Long guestSessionId, Long reportId) {
        return fortuneReportRepository.findByIdAndGuestSessionId(reportId, guestSessionId)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
    }

    private GuestSession getValidGuestSession(String guestId) {
        return getValidGuestSession(guestId, false);
    }

    private GuestSession getValidGuestSessionForUpdate(String guestId) {
        return getValidGuestSession(guestId, true);
    }

    private GuestSession getValidGuestSession(String guestId, boolean forUpdate) {
        if (guestId == null || guestId.isBlank()) {
            throw new CustomException(FortuneReportErrorCode.GUEST_COOKIE_REQUIRED);
        }

        GuestSession guestSession = (forUpdate
                ? guestSessionRepository.findForUpdateByGuestId(guestId)
                : guestSessionRepository.findByGuestId(guestId))
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.GUEST_SESSION_NOT_FOUND));

        if (!guestSession.isUsable(LocalDateTime.now())) {
            throw new CustomException(FortuneReportErrorCode.GUEST_SESSION_UNAVAILABLE);
        }
        return guestSession;
    }
}
