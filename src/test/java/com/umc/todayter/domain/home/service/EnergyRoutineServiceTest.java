package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ActionItem;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ComplementActionGuide;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.exception.code.FortuneReportErrorCode;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.fortune.service.provider.ComplementActionProvider;
import com.umc.todayter.domain.home.dto.response.EnergyRoutinesResponse;
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
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyRoutineServiceTest {

    @Mock
    private FortuneReportRepository fortuneReportRepository;

    @Mock
    private FortuneReportResultParser fortuneReportResultParser;

    @Mock
    private ComplementActionProvider complementActionProvider;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void memberEnergyRoutinesUseComplementElementAndReportId() {
        Member member = Member.create("member");
        FortuneReport report = report(99L, "report-content");
        BasicReport basic = basic(List.of(FiveElement.FIRE, FiveElement.WOOD), FiveElement.WATER);
        ComplementActionGuide guide = guide(FiveElement.WATER,
                new ActionItem(1, "물가", "강이나 하천을 따라 천천히 걸어봐요"),
                new ActionItem(2, "고요", "도서관에서 시간을 보내봐요"),
                new ActionItem(3, "사유", "떠오르는 감정을 짧게 적어봐요")
        );

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                1L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(fortuneReportResultParser.parseBasic("report-content")).thenReturn(basic);
        when(complementActionProvider.select(FiveElement.WATER, 99L)).thenReturn(guide);

        EnergyRoutinesResponse response = service().getEnergyRoutines(CurrentUserContext.forMember(1L));

        assertThat(response.element().code()).isEqualTo(FiveElement.WATER);
        assertThat(response.element().name()).isEqualTo(FiveElement.WATER.getLabel());
        assertThat(response.routines()).hasSize(3);
        assertThat(response.routines()).extracting("order").containsExactly(1, 2, 3);
        assertThat(response.routines()).extracting("type").containsExactly("물가", "고요", "사유");
        assertThat(response.routines()).extracting("text").containsExactly(
                "강이나 하천을 따라 천천히 걸어봐요",
                "도서관에서 시간을 보내봐요",
                "떠오르는 감정을 짧게 적어봐요"
        );

        verify(memberRepository).findById(1L);
        verify(fortuneReportRepository).findFirstByMemberIdAndStatusOrderByIdDesc(
                1L, FortuneReportStatus.COMPLETED
        );
        verify(fortuneReportResultParser).parseBasic("report-content");
        verify(complementActionProvider).select(FiveElement.WATER, 99L);
        verify(complementActionProvider, never()).select(eq(FiveElement.FIRE), anyLong());
    }

    @Test
    void guestEnergyRoutinesUseLatestCompletedReportWithoutMemberLookup() {
        FortuneReport report = report(100L, "guest-report-content");
        BasicReport basic = basic(List.of(FiveElement.FIRE), FiveElement.WATER);
        ComplementActionGuide guide = guide(FiveElement.WATER,
                new ActionItem(1, "물가", "text")
        );

        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(fortuneReportResultParser.parseBasic("guest-report-content")).thenReturn(basic);
        when(complementActionProvider.select(FiveElement.WATER, 100L)).thenReturn(guide);

        EnergyRoutinesResponse response = service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id"));

        assertThat(response.element().code()).isEqualTo(FiveElement.WATER);
        assertThat(response.routines()).hasSize(1);
        verifyNoInteractions(memberRepository);
        verify(fortuneReportRepository).findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        );
        verify(fortuneReportResultParser).parseBasic("guest-report-content");
        verify(complementActionProvider).select(FiveElement.WATER, 100L);
    }

    @Test
    void missingMemberThrowsMemberNotFoundWithoutReportLookup() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forMember(1L)),
                MemberErrorCode.MEMBER_NOT_FOUND
        );
        verifyNoInteractions(fortuneReportRepository, fortuneReportResultParser, complementActionProvider);
    }

    @Test
    void inactiveMemberThrowsMemberInactiveWithoutReportLookup() {
        Member member = Member.create("member");
        ReflectionTestUtils.setField(member, "status", MemberStatus.WITHDRAWN);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forMember(1L)),
                MemberErrorCode.MEMBER_INACTIVE
        );
        verifyNoInteractions(fortuneReportRepository, fortuneReportResultParser, complementActionProvider);
    }

    @Test
    void missingMemberReportThrowsReportNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(Member.create("member")));
        when(fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                1L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.empty());

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forMember(1L)),
                FortuneReportErrorCode.REPORT_NOT_FOUND
        );
        verifyNoInteractions(fortuneReportResultParser, complementActionProvider);
    }

    @Test
    void missingGuestReportThrowsReportNotFound() {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.empty());

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_NOT_FOUND
        );
        verifyNoInteractions(memberRepository, fortuneReportResultParser, complementActionProvider);
    }

    @Test
    void invalidReportContentThrowsReportContentUnavailable() {
        assertInvalidReportContent(null);
        assertInvalidReportContent("");
        assertInvalidReportContent("   ");
    }

    @Test
    void invalidParsedBasicThrowsReportContentUnavailable() {
        assertInvalidBasic(null);
        assertInvalidBasic(basic(List.of(FiveElement.FIRE), null));
    }

    @Test
    void complementElementNullDoesNotCallProvider() {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report(100L, "report-content")));
        when(fortuneReportResultParser.parseBasic("report-content"))
                .thenReturn(basic(List.of(FiveElement.FIRE), null));

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
        verifyNoInteractions(complementActionProvider);
    }

    @Test
    void malformedSectionNumberThrowsReportContentUnavailable() {
        String reportContent = """
                ## 999999999999999999999999. 종합

                ### 보완 오행
                보완 오행: 수
                """;
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report(100L, reportContent)));

        assertError(
                () -> serviceWithParser(new FortuneReportResultParser(new ObjectMapper()))
                        .getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
        verifyNoInteractions(complementActionProvider);
    }

    @Test
    void invalidProviderResultThrowsReportContentUnavailable() {
        assertInvalidProviderResult(null);
        assertInvalidProviderResult(new ComplementActionGuide(null, null, List.of()));
        assertInvalidProviderResult(new ComplementActionGuide(FiveElement.WATER, FiveElement.WATER.getLabel(), null));
        assertInvalidProviderResult(new ComplementActionGuide(FiveElement.WATER, FiveElement.WATER.getLabel(), List.of()));
    }

    @Test
    void nullReportIdThrowsReportContentUnavailableWithoutCallingProvider() {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report(null, "report-content")));
        when(fortuneReportResultParser.parseBasic("report-content"))
                .thenReturn(basic(List.of(FiveElement.FIRE), FiveElement.WATER));

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
        verifyNoInteractions(complementActionProvider);
    }

    @Test
    void limitsRoutinesToThreeItems() {
        FortuneReport report = report(100L, "report-content");
        BasicReport basic = basic(List.of(FiveElement.FIRE), FiveElement.WATER);
        ComplementActionGuide guide = guide(FiveElement.WATER,
                new ActionItem(1, "type1", "text1"),
                new ActionItem(2, "type2", "text2"),
                new ActionItem(3, "type3", "text3"),
                new ActionItem(4, "type4", "text4")
        );

        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report));
        when(fortuneReportResultParser.parseBasic("report-content")).thenReturn(basic);
        when(complementActionProvider.select(FiveElement.WATER, 100L)).thenReturn(guide);

        EnergyRoutinesResponse response = service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id"));

        assertThat(response.routines()).hasSize(3);
        assertThat(response.routines()).extracting("order").containsExactly(1, 2, 3);
    }

    private void assertInvalidReportContent(String reportContent) {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report(100L, reportContent)));

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
        verifyNoInteractions(fortuneReportResultParser, complementActionProvider);
    }

    private void assertInvalidBasic(BasicReport basic) {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report(100L, "report-content")));
        when(fortuneReportResultParser.parseBasic("report-content")).thenReturn(basic);

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
    }

    private void assertInvalidProviderResult(ComplementActionGuide guide) {
        when(fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                10L, FortuneReportStatus.COMPLETED
        )).thenReturn(Optional.of(report(100L, "report-content")));
        when(fortuneReportResultParser.parseBasic("report-content"))
                .thenReturn(basic(List.of(FiveElement.FIRE), FiveElement.WATER));
        when(complementActionProvider.select(FiveElement.WATER, 100L)).thenReturn(guide);

        assertError(
                () -> service().getEnergyRoutines(CurrentUserContext.forGuest(10L, "guest-id")),
                FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE
        );
    }

    private void assertError(Runnable action, Object errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(errorCode));
    }

    private EnergyRoutineService service() {
        return serviceWithParser(fortuneReportResultParser);
    }

    private EnergyRoutineService serviceWithParser(FortuneReportResultParser parser) {
        return new EnergyRoutineService(
                fortuneReportRepository,
                parser,
                complementActionProvider,
                memberRepository
        );
    }

    private FortuneReport report(Long id, String reportContent) {
        try {
            Constructor<FortuneReport> constructor = FortuneReport.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FortuneReport report = constructor.newInstance();
            ReflectionTestUtils.setField(report, "id", id);
            ReflectionTestUtils.setField(report, "reportContent", reportContent);
            return report;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private BasicReport basic(List<FiveElement> primaryElements, FiveElement complementElement) {
        return new BasicReport(
                "type title",
                "type name",
                "summary",
                primaryElements,
                complementElement,
                List.of(),
                List.of()
        );
    }

    private ComplementActionGuide guide(FiveElement element, ActionItem... actions) {
        return new ComplementActionGuide(
                element,
                element.getLabel(),
                List.of(actions)
        );
    }
}
