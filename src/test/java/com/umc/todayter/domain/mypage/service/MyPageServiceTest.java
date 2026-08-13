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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long REPORT_ID = 10L;

    @Mock
    private MemberService memberService;

    @Mock
    private SocialAccountQueryService socialAccountQueryService;

    @Mock
    private FortuneReportRepository fortuneReportRepository;

    @Mock
    private FortuneReportResultParser fortuneReportResultParser;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    void getMyPageReturnsLatestCompletedReportSummary() {
        Member member = Member.create("오늘이");
        FortuneReport report = mock(FortuneReport.class);
        BasicReport basic = new BasicReport(
                null,
                null,
                null,
                List.of(FiveElement.WATER, FiveElement.WOOD),
                FiveElement.FIRE,
                List.of(),
                List.of()
        );

        when(memberService.getActiveMember(MEMBER_ID)).thenReturn(member);
        when(socialAccountQueryService.getSocialAccounts(MEMBER_ID))
                .thenReturn(new SocialAccountListResponse(List.of()));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                MEMBER_ID, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(report.getId()).thenReturn(REPORT_ID);
        when(report.getReportContent()).thenReturn("report content");
        when(fortuneReportResultParser.parseBasic("report content")).thenReturn(basic);

        MyPageResponseDTO.MainDTO result = myPageService.getMyPage(MEMBER_ID);

        assertThat(result.getReportId()).isEqualTo(REPORT_ID);
        assertThat(result.getNickname()).isEqualTo("오늘이");
        assertThat(result.getMainElement()).isEqualTo("수");
        assertThat(result.getComplementaryElement()).isEqualTo("화");
    }

    @Test
    void getMyPageReturnsProfileWithoutReportDataWhenCompletedReportDoesNotExist() {
        Member member = Member.create("오늘이");
        when(memberService.getActiveMember(MEMBER_ID)).thenReturn(member);
        when(socialAccountQueryService.getSocialAccounts(MEMBER_ID))
                .thenReturn(new SocialAccountListResponse(List.of()));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                MEMBER_ID, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.empty());

        MyPageResponseDTO.MainDTO result = myPageService.getMyPage(MEMBER_ID);

        assertThat(result.getReportId()).isNull();
        assertThat(result.getNickname()).isEqualTo("오늘이");
        assertThat(result.getMainElement()).isNull();
        assertThat(result.getComplementaryElement()).isNull();
        verifyNoInteractions(fortuneReportResultParser);
    }

    @Test
    void getMyPageStillReturnsProfileWhenCompletedReportContentIsBlank() {
        Member member = Member.create("오늘이");
        FortuneReport report = mock(FortuneReport.class);
        when(memberService.getActiveMember(MEMBER_ID)).thenReturn(member);
        when(socialAccountQueryService.getSocialAccounts(MEMBER_ID))
                .thenReturn(new SocialAccountListResponse(List.of()));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                MEMBER_ID, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(report.getId()).thenReturn(REPORT_ID);
        when(report.getReportContent()).thenReturn(null);

        MyPageResponseDTO.MainDTO result = myPageService.getMyPage(MEMBER_ID);

        assertThat(result.getReportId()).isEqualTo(REPORT_ID);
        assertThat(result.getNickname()).isEqualTo("오늘이");
        assertThat(result.getMainElement()).isNull();
        assertThat(result.getComplementaryElement()).isNull();
        verifyNoInteractions(fortuneReportResultParser);
    }

    @Test
    void getMyPageStillReturnsProfileWhenReportContentCannotBeParsed() {
        Member member = Member.create("오늘이");
        FortuneReport report = mock(FortuneReport.class);
        when(memberService.getActiveMember(MEMBER_ID)).thenReturn(member);
        when(socialAccountQueryService.getSocialAccounts(MEMBER_ID))
                .thenReturn(new SocialAccountListResponse(List.of()));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                MEMBER_ID, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(report.getId()).thenReturn(REPORT_ID);
        when(report.getReportContent()).thenReturn("invalid report content");
        when(fortuneReportResultParser.parseBasic("invalid report content"))
                .thenThrow(new IllegalArgumentException("invalid report"));

        MyPageResponseDTO.MainDTO result = myPageService.getMyPage(MEMBER_ID);

        assertThat(result.getReportId()).isEqualTo(REPORT_ID);
        assertThat(result.getNickname()).isEqualTo("오늘이");
        assertThat(result.getMainElement()).isNull();
        assertThat(result.getComplementaryElement()).isNull();
    }

    @Test
    void getSocialConnectionsDelegatesToMemberDomain() {
        SocialAccountListResponse expected = new SocialAccountListResponse(List.of());
        when(socialAccountQueryService.getSocialAccounts(MEMBER_ID)).thenReturn(expected);

        SocialAccountListResponse result = myPageService.getSocialConnections(MEMBER_ID);

        assertThat(result).isSameAs(expected);
    }
}
