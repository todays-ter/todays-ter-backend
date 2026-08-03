package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.exception.code.FortuneReportErrorCode;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.home.dto.response.TodayEnergyElementResponse;
import com.umc.todayter.domain.home.dto.response.TodayEnergyResponse;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.context.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayEnergyService {

    private final FortuneReportRepository fortuneReportRepository;
    private final FortuneReportResultParser fortuneReportResultParser;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public TodayEnergyResponse getTodayEnergy(CurrentUserContext context) {
        FortuneReport report = context.isMember()
                ? getMemberLatestCompletedReport(context.memberId())
                : getGuestLatestCompletedReport(context.guestSessionId());

        BasicReport basic = parseBasic(report);
        FiveElement representativeElement = representativeElementOf(basic);
        String description = descriptionOf(basic);

        return new TodayEnergyResponse(
                LocalDate.now(clock),
                TodayEnergyElementResponse.from(representativeElement),
                description
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

    private FiveElement representativeElementOf(BasicReport basic) {
        List<FiveElement> primaryElements = basic.primaryElements();
        if (primaryElements == null || primaryElements.isEmpty() || primaryElements.get(0) == null) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }
        return primaryElements.get(0);
    }

    private String descriptionOf(BasicReport basic) {
        String description = basic.elementSummary();
        if (!StringUtils.hasText(description)) {
            throw new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE);
        }
        return description;
    }
}
