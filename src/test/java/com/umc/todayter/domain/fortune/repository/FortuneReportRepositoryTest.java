package com.umc.todayter.domain.fortune.repository;

import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.enums.FortuneReportStep;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FortuneReportRepositoryTest {

    @Autowired
    private FortuneReportRepository fortuneReportRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findFirstByMemberIdOrderByCreatedAtDescIdDescSelectsLatestCreatedAt() {
        FortuneReport oldReport = fortuneReportRepository.save(reportForMember(1L));
        FortuneReport latestReport = fortuneReportRepository.save(reportForMember(1L));
        fortuneReportRepository.save(reportForMember(2L));
        fortuneReportRepository.flush();
        setCreatedAt(oldReport, LocalDateTime.of(2026, 8, 1, 0, 0));
        setCreatedAt(latestReport, LocalDateTime.of(2026, 8, 2, 0, 0));

        assertThat(fortuneReportRepository.findFirstByMemberIdOrderByCreatedAtDescIdDesc(1L))
                .map(FortuneReport::getId)
                .contains(latestReport.getId());
    }

    @Test
    void findFirstByGuestSessionIdOrderByCreatedAtDescIdDescUsesIdAsTieBreaker() {
        FortuneReport first = fortuneReportRepository.save(reportForGuest(10L));
        FortuneReport second = fortuneReportRepository.save(reportForGuest(10L));
        fortuneReportRepository.save(reportForGuest(20L));
        fortuneReportRepository.flush();
        LocalDateTime sameCreatedAt = LocalDateTime.of(2026, 8, 1, 0, 0);
        setCreatedAt(first, sameCreatedAt);
        setCreatedAt(second, sameCreatedAt);

        assertThat(first.getId()).isLessThan(second.getId());
        assertThat(fortuneReportRepository.findFirstByGuestSessionIdOrderByCreatedAtDescIdDesc(10L))
                .map(FortuneReport::getId)
                .contains(second.getId());
    }

    @Test
    void latestLookupDoesNotFilterStatus() {
        FortuneReport completed = fortuneReportRepository.save(reportForMember(1L, FortuneReportStatus.COMPLETED));
        FortuneReport processing = fortuneReportRepository.save(reportForMember(1L, FortuneReportStatus.PROCESSING));
        fortuneReportRepository.flush();
        setCreatedAt(completed, LocalDateTime.of(2026, 8, 1, 0, 0));
        setCreatedAt(processing, LocalDateTime.of(2026, 8, 2, 0, 0));

        assertThat(fortuneReportRepository.findFirstByMemberIdOrderByCreatedAtDescIdDesc(1L))
                .map(FortuneReport::getId)
                .contains(processing.getId());
    }

    private void setCreatedAt(FortuneReport report, LocalDateTime createdAt) {
        entityManager.createNativeQuery("update fortune_reports set created_at = ? where id = ?")
                .setParameter(1, createdAt)
                .setParameter(2, report.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    private FortuneReport reportForMember(Long memberId) {
        return reportForMember(memberId, FortuneReportStatus.COMPLETED);
    }

    private FortuneReport reportForMember(Long memberId, FortuneReportStatus status) {
        FortuneReport report = report(status);
        ReflectionTestUtils.setField(report, "memberId", memberId);
        return report;
    }

    private FortuneReport reportForGuest(Long guestSessionId) {
        FortuneReport report = report(FortuneReportStatus.COMPLETED);
        ReflectionTestUtils.setField(report, "guestSessionId", guestSessionId);
        return report;
    }

    private FortuneReport report(FortuneReportStatus status) {
        try {
            Constructor<FortuneReport> constructor = FortuneReport.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FortuneReport report = constructor.newInstance();
            ReflectionTestUtils.setField(report, "onboardingId", 1L);
            ReflectionTestUtils.setField(report, "status", status);
            ReflectionTestUtils.setField(report, "currentStep", FortuneReportStep.WAITING);
            ReflectionTestUtils.setField(report, "progress", 0);
            ReflectionTestUtils.setField(report, "retryCount", 0);
            return report;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
