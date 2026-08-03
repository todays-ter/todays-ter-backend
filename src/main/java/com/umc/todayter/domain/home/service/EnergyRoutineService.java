package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ComplementActionGuide;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.exception.code.FortuneReportErrorCode;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.fortune.service.provider.ComplementActionProvider;
import com.umc.todayter.domain.home.dto.response.EnergyRoutineElementResponse;
import com.umc.todayter.domain.home.dto.response.EnergyRoutineItemResponse;
import com.umc.todayter.domain.home.dto.response.EnergyRoutinesResponse;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.context.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnergyRoutineService {

    private final FortuneReportRepository fortuneReportRepository;
    private final FortuneReportResultParser fortuneReportResultParser;
    private final ComplementActionProvider complementActionProvider;
    private final MemberRepository memberRepository;

    public EnergyRoutinesResponse getEnergyRoutines(CurrentUserContext context) {
        FortuneReport report = context.isMember()
                ? getMemberLatestCompletedReport(context.memberId())
                : getGuestLatestCompletedReport(context.guestSessionId());

        BasicReport basic = parseBasic(report);
        FiveElement complementElement = complementElementOf(basic);
        ComplementActionGuide guide = selectGuide(complementElement, report);

        List<EnergyRoutineItemResponse> routines = guide.actions().stream()
                .limit(3)
                .map(EnergyRoutineItemResponse::from)
                .toList();

        return new EnergyRoutinesResponse(
                EnergyRoutineElementResponse.from(complementElement),
                routines
        );
    }

    private FortuneReport getMemberLatestCompletedReport(Long memberId) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!member.isActive()) {
            throw new CustomException(MemberErrorCode.MEMBER_INACTIVE);
        }

        return fortuneReportRepository
                .findFirstByMemberIdAndStatusOrderByIdDesc(memberId, FortuneReportStatus.COMPLETED)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
    }

    private FortuneReport getGuestLatestCompletedReport(Long guestSessionId) {
        return fortuneReportRepository
                .findFirstByGuestSessionIdAndStatusOrderByIdDesc(guestSessionId, FortuneReportStatus.COMPLETED)
                .orElseThrow(() -> new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));
    }

    private BasicReport parseBasic(FortuneReport report) {
        String reportContent = report.getReportContent();
        if (!StringUtils.hasText(reportContent)) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }

        BasicReport basic = fortuneReportResultParser.parseBasic(reportContent);
        if (basic == null) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }
        return basic;
    }

    private FiveElement complementElementOf(BasicReport basic) {
        FiveElement complementElement = basic.complementElement();
        if (complementElement == null) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }
        return complementElement;
    }

    private ComplementActionGuide selectGuide(FiveElement complementElement, FortuneReport report) {
        Long reportId = report.getId();
        if (reportId == null) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }

        ComplementActionGuide guide = complementActionProvider.select(complementElement, reportId);
        if (guide == null
                || guide.element() == null
                || guide.actions() == null
                || guide.actions().isEmpty()) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }
        return guide;
    }
}
