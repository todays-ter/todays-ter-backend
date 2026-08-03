package com.umc.todayter.domain.fortune.entity;

import tools.jackson.databind.ObjectMapper;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.enums.FortuneReportStep;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.CalendarType;
import com.umc.todayter.domain.onboarding.enums.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FortuneReportTest {

    @Test
    void progressCompletesInDefinedStages() {
        FortuneReport report = createReport();

        report.startAttempt();
        report.advance(FortuneReportStep.BIRTH_DATA_PREPARED, 20);
        report.saveManseData(new ObjectMapper().createObjectNode());
        assertThat(report.getManseData()).isEqualTo("{}");
        report.advance(FortuneReportStep.PROMPT_PREPARED, 60);
        report.saveAiReport("report");
        report.complete();

        assertThat(report.getStatus()).isEqualTo(FortuneReportStatus.COMPLETED);
        assertThat(report.getProgress()).isEqualTo(100);
        assertThat(report.getCurrentStep()).isEqualTo(FortuneReportStep.COMPLETED);
        assertThat(report.getReportContent()).isEqualTo("report");
    }

    @Test
    void retryResetsFailureAndProgress() {
        FortuneReport report = createReport();
        report.startAttempt();
        report.advance(FortuneReportStep.BIRTH_DATA_PREPARED, 20);
        report.saveManseData(new ObjectMapper().createObjectNode());
        report.fail("SAZU_API_FAILED", "실패");

        report.prepareRetry();

        assertThat(report.getStatus()).isEqualTo(FortuneReportStatus.PROCESSING);
        assertThat(report.getProgress()).isZero();
        assertThat(report.getRetryCount()).isEqualTo(1);
        assertThat(report.getFailureCode()).isNull();
        assertThat(report.getManseData()).isEqualTo("{}");
    }

    private FortuneReport createReport() {
        Onboarding onboarding = mock(Onboarding.class);
        when(onboarding.getId()).thenReturn(10L);
        when(onboarding.getCalendarType()).thenReturn(CalendarType.SOLAR);
        when(onboarding.getGender()).thenReturn(Gender.FEMALE);
        when(onboarding.getBirthDate()).thenReturn(LocalDate.of(2000, 1, 1));
        when(onboarding.isBirthTimeUnknown()).thenReturn(true);
        when(onboarding.getConcernTypes()).thenReturn(List.of());
        return FortuneReport.createForMember(1L, onboarding);
    }
}
