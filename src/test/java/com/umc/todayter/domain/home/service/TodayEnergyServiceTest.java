package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.exception.code.FortuneReportErrorCode;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.home.dto.response.TodayEnergyResponse;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.context.CurrentUserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodayEnergyServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-03T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private FortuneReportRepository fortuneReportRepository;

    @Mock
    private FortuneReportResultParser fortuneReportResultParser;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void memberTodayEnergyUsesLatestCompletedReport() {
        Member member = Member.create("member");
        FortuneReport report = report("report-content");
        BasicReport basic = basic("summary", List.of(FiveElement.FIRE, FiveElement.WOOD), FiveElement.WATER);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                1L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(fortuneReportResultParser.parseBasic("report-content")).thenReturn(basic);

        TodayEnergyResponse response = service().getTodayEnergy(CurrentUserContext.forMember(1L));

        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(response.element().code()).isEqualTo(FiveElement.FIRE);
        assertThat(response.element().name()).isEqualTo(FiveElement.FIRE.getLabel());
        assertThat(response.description()).isEqualTo("summary");
        verify(memberRepository).findById(1L);
        verify(fortuneReportRepository).findFirstByMemberIdAndStatusOrderByIdDesc(
                1L, FortuneReportStatus.COMPLETED
        );
        verify(fortuneReportResultParser).parseBasic("report-content");
    }

    @Test
    void guestTodayEnergyUsesLatestCompletedReportWithoutMemberLookup() {
        FortuneReport report = report("guest-report-content");
        BasicReport basic = basic("guest summary", List.of(FiveElement.WATER), FiveElement.FIRE);

        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(fortuneReportResultParser.parseBasic("guest-report-content")).thenReturn(basic);

        TodayEnergyResponse response = service().getTodayEnergy(CurrentUserContext.forGuest(10L, "guest-id"));

        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(response.element().code()).isEqualTo(FiveElement.WATER);
        assertThat(response.element().name()).isEqualTo(FiveElement.WATER.getLabel());
        assertThat(response.description()).isEqualTo("guest summary");
        verifyNoInteractions(memberRepository);
        verify(fortuneReportRepository).findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        );
    }

    @Test
    void missingMemberThrowsMemberNotFoundWithoutGuestFallback() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertError(
                () -> service().getTodayEnergy(CurrentUserContext.forMember(1L)),
                MemberErrorCode.MEMBER_NOT_FOUND
        );
        verifyNoInteractions(fortuneReportRepository);
    }

    @Test
    void inactiveMemberThrowsMemberInactiveWithoutGuestFallback() {
        Member member = Member.create("member");
        ReflectionTestUtils.setField(member, "status", MemberStatus.WITHDRAWN);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertError(
                () -> service().getTodayEnergy(CurrentUserContext.forMember(1L)),
                MemberErrorCode.MEMBER_INACTIVE
        );
        verifyNoInteractions(fortuneReportRepository);
    }

    @Test
    void missingMemberReportThrowsReportNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(Member.create("member")));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                1L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.empty());

        assertError(
                () -> service().getTodayEnergy(CurrentUserContext.forMember(1L)),
                FortuneReportErrorCode.REPORT_NOT_FOUND
        );
    }

    @Test
    void missingGuestReportThrowsReportNotFound() {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.empty());

        assertError(
                () -> service().getTodayEnergy(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_NOT_FOUND
        );
    }

    @Test
    void blankReportContentThrowsReportContentUnavailable() {
        assertInvalidReportContent(null);
        assertInvalidReportContent("");
        assertInvalidReportContent("   ");
    }

    @Test
    void invalidParsedBasicThrowsReportContentUnavailable() {
        assertInvalidBasic(null);
        assertInvalidBasic(basic("summary", null, FiveElement.WATER));
        assertInvalidBasic(basic("summary", List.of(), FiveElement.WATER));
        assertInvalidBasic(basic("summary", Collections.singletonList(null), FiveElement.WATER));
        assertInvalidBasic(basic(null, List.of(FiveElement.FIRE), FiveElement.WATER));
        assertInvalidBasic(basic("", List.of(FiveElement.FIRE), FiveElement.WATER));
        assertInvalidBasic(basic("   ", List.of(FiveElement.FIRE), FiveElement.WATER));
    }

    @Test
    void representativeElementUsesFirstPrimaryElementNotComplementElement() {
        FortuneReport report = report("report-content");
        BasicReport basic = basic("summary", List.of(FiveElement.FIRE, FiveElement.WOOD), FiveElement.WATER);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(Member.create("member")));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                1L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(fortuneReportResultParser.parseBasic("report-content")).thenReturn(basic);

        TodayEnergyResponse response = service().getTodayEnergy(CurrentUserContext.forMember(1L));

        assertThat(response.element().code()).isEqualTo(FiveElement.FIRE);
    }

    private void assertInvalidReportContent(String reportContent) {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report(reportContent)));

        assertError(
                () -> service().getTodayEnergy(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
    }

    private void assertInvalidBasic(BasicReport basic) {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report("report-content")));
        when(fortuneReportResultParser.parseBasic("report-content")).thenReturn(basic);

        assertError(
                () -> service().getTodayEnergy(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
    }

    private void assertError(Runnable action, Object errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(errorCode));
    }

    private TodayEnergyService service() {
        return new TodayEnergyService(
                fortuneReportRepository,
                fortuneReportResultParser,
                memberRepository,
                FIXED_CLOCK
        );
    }

    private FortuneReport report(String reportContent) {
        try {
            Constructor<FortuneReport> constructor = FortuneReport.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FortuneReport report = constructor.newInstance();
            ReflectionTestUtils.setField(report, "reportContent", reportContent);
            return report;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private BasicReport basic(String elementSummary, List<FiveElement> primaryElements, FiveElement complementElement) {
        return new BasicReport(
                "type title",
                "type name",
                elementSummary,
                primaryElements,
                complementElement,
                List.of(),
                List.of()
        );
    }
}
