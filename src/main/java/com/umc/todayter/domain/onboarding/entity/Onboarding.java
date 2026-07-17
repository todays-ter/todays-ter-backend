package com.umc.todayter.domain.onboarding.entity;

import com.umc.todayter.domain.onboarding.dto.request.GuestSajuRequest;
import com.umc.todayter.domain.onboarding.enums.CalendarType;
import com.umc.todayter.domain.onboarding.enums.OnboardingStep;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "onboardings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_onboardings_guest_session_id",
                        columnNames = "guest_session_id"
                )
        }
)
public class Onboarding extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_session_id")
    private GuestSession guestSession;

    // 사용자는 하나의 온보딩 데이터만 가짐
    @Column(name = "member_id", unique = true)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type")
    private CalendarType calendarType;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Column(name = "birth_time_unknown", nullable = false)
    private boolean birthTimeUnknown;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_step", nullable = false)
    private OnboardingStep onboardingStep;

    private Onboarding(GuestSession guestSession) {
        this.guestSession = guestSession;
        this.birthTimeUnknown = false;
        this.onboardingStep = OnboardingStep.STARTED;
    }

    // 비회원용 온보딩 객체 생성
    public static Onboarding createForGuest(GuestSession guestSession) {
        return new Onboarding(guestSession);
    }

    public void updateSaju(GuestSajuRequest request) {
        this.calendarType = request.calendarType();
        this.birthDate = request.birthDate();
        this.birthTime = request.birthTime();
        this.birthTimeUnknown = request.birthTimeUnknown();
        this.onboardingStep = OnboardingStep.SAJU_COMPLETED;
    }
}
