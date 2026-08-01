package com.umc.todayter.domain.fortune.entity;

import tools.jackson.databind.JsonNode;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.enums.FortuneReportStep;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.CalendarType;
import com.umc.todayter.domain.onboarding.enums.ConcernType;
import com.umc.todayter.domain.onboarding.enums.Gender;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fortune_reports", indexes = {
        @Index(name = "idx_fortune_reports_member_status", columnList = "member_id,status"),
        @Index(name = "idx_fortune_reports_guest_status", columnList = "guest_session_id,status")
}, check = @CheckConstraint(
        name = "chk_fortune_reports_single_owner",
        constraint = "(member_id is null) <> (guest_session_id is null)"
))
public class FortuneReport extends BaseEntity {

    @Version
    private Long version;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "guest_session_id")
    private Long guestSessionId;

    @Column(name = "onboarding_id", nullable = false)
    private Long onboardingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", nullable = false, length = 10)
    private CalendarType calendarType;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Column(name = "birth_time_unknown", nullable = false)
    private boolean birthTimeUnknown;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "concern_types", columnDefinition = "json", nullable = false)
    private List<ConcernType> concernTypes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FortuneReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 30)
    private FortuneReportStep currentStep;

    @Column(nullable = false)
    private int progress;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "manse_data", columnDefinition = "json")
    private String manseData;

    @Column(name = "report_content", columnDefinition = "LONGTEXT")
    private String reportContent;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 255)
    private String failureMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    public static FortuneReport createForMember(Long memberId, Onboarding onboarding) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        FortuneReport report = createFrom(onboarding);
        report.memberId = memberId;
        return report;
    }

    public static FortuneReport createForGuest(Long guestSessionId, Onboarding onboarding) {
        if (guestSessionId == null) {
            throw new IllegalArgumentException("게스트 세션 ID는 필수입니다.");
        }
        FortuneReport report = createFrom(onboarding);
        report.guestSessionId = guestSessionId;
        return report;
    }

    private static FortuneReport createFrom(Onboarding onboarding) {
        FortuneReport report = new FortuneReport();
        report.onboardingId = onboarding.getId();
        report.gender = onboarding.getGender();
        report.calendarType = onboarding.getCalendarType();
        report.birthDate = onboarding.getBirthDate();
        report.birthTime = onboarding.getBirthTime();
        report.birthTimeUnknown = onboarding.isBirthTimeUnknown();
        report.concernTypes = new ArrayList<>(onboarding.getConcernTypes());
        report.status = FortuneReportStatus.PROCESSING;
        report.currentStep = FortuneReportStep.QUEUED;
        report.progress = 0;
        report.retryCount = 0;
        return report;
    }

    public void transferToMember(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        this.memberId = memberId;
        this.guestSessionId = null;
    }

    public void startAttempt() {
        status = FortuneReportStatus.PROCESSING;
        currentStep = FortuneReportStep.QUEUED;
        progress = 0;
        failureCode = null;
        failureMessage = null;
        failedAt = null;
        completedAt = null;
        startedAt = LocalDateTime.now();
    }

    public void prepareRetry() {
        retryCount++;
        reportContent = null;
        startAttempt();
    }

    public void advance(FortuneReportStep step, int progress) {
        if (status != FortuneReportStatus.PROCESSING) {
            throw new IllegalStateException("처리 중인 리포트만 진행 상태를 변경할 수 있습니다.");
        }
        if (progress < this.progress || progress < 0 || progress > 100) {
            throw new IllegalArgumentException("진행률은 감소할 수 없으며 0부터 100 사이여야 합니다.");
        }
        this.currentStep = step;
        this.progress = progress;
    }

    public void saveManseData(JsonNode manseData) {
        this.manseData = manseData == null ? null : manseData.toString();
        advance(FortuneReportStep.MANSE_DATA_CREATED, 40);
    }

    public void saveAiReport(String reportContent) {
        this.reportContent = reportContent;
        advance(FortuneReportStep.AI_REPORT_CREATED, 80);
    }

    public void complete() {
        status = FortuneReportStatus.COMPLETED;
        currentStep = FortuneReportStep.COMPLETED;
        progress = 100;
        completedAt = LocalDateTime.now();
    }

    public void fail(String failureCode, String failureMessage) {
        status = FortuneReportStatus.FAILED;
        currentStep = FortuneReportStep.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        failedAt = LocalDateTime.now();
    }
}
