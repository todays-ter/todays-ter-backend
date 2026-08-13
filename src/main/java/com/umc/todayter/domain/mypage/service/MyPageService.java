package com.umc.todayter.domain.mypage.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.member.dto.response.SocialAccountListResponse;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.service.MemberService;
import com.umc.todayter.domain.member.service.SocialAccountQueryService;
import com.umc.todayter.domain.mypage.dto.MyPageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberService memberService;
    private final SocialAccountQueryService socialAccountQueryService;
    private final FortuneReportRepository fortuneReportRepository;
    private final FortuneReportResultParser fortuneReportResultParser;

    public MyPageResponseDTO.MainDTO getMyPage(Long memberId) {
        Member member = memberService.getActiveMember(memberId);
        String loginProvider = getLoginProvider(memberId);
        FortuneReport latestReport = fortuneReportRepository
                .findFirstByMemberIdAndStatusOrderByIdDesc(memberId, FortuneReportStatus.COMPLETED)
                .orElse(null);

        if (latestReport == null) {
            return createMainResponse(member, loginProvider, null, null, null);
        }

        ElementInfo elementInfo = extractElementInfo(latestReport.getReportContent());
        return createMainResponse(member, loginProvider, latestReport.getId(),
                elementInfo.mainElement(), elementInfo.complementaryElement());
    }

    public SocialAccountListResponse getSocialConnections(Long memberId) {
        return socialAccountQueryService.getSocialAccounts(memberId);
    }

    private MyPageResponseDTO.MainDTO createMainResponse(
            Member member,
            String loginProvider,
            Long reportId,
            FiveElement mainElement,
            FiveElement complementaryElement
    ) {
        return MyPageResponseDTO.MainDTO.builder()
                .reportId(reportId)
                .nickname(member.getNickname())
                .profileImageUrl(null)
                .userId(member.getId())
                .loginProvider(loginProvider)
                .mainElement(toLabel(mainElement))
                .complementaryElement(toLabel(complementaryElement))
                .build();
    }

    private String getLoginProvider(Long memberId) {
        return socialAccountQueryService.getSocialAccounts(memberId).socialAccounts().stream()
                .findFirst()
                .map(account -> account.provider().name())
                .orElse(null);
    }

    private ElementInfo extractElementInfo(String reportContent) {
        if (reportContent == null || reportContent.isBlank()) {
            return ElementInfo.empty();
        }

        try {
            BasicReport basic = fortuneReportResultParser.parseBasic(reportContent);
            List<FiveElement> primaryElements = basic.primaryElements();
            FiveElement mainElement = primaryElements == null || primaryElements.isEmpty()
                    ? null
                    : primaryElements.get(0);
            return new ElementInfo(mainElement, basic.complementElement());
        } catch (RuntimeException exception) {
            log.warn("마이페이지 리포트 오행 정보를 파싱할 수 없습니다.", exception);
            return ElementInfo.empty();
        }
    }

    private String toLabel(FiveElement element) {
        return element == null ? null : element.getLabel();
    }

    private record ElementInfo(FiveElement mainElement, FiveElement complementaryElement) {
        private static ElementInfo empty() {
            return new ElementInfo(null, null);
        }
    }
}
