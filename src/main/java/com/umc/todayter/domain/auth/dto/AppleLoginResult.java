package com.umc.todayter.domain.auth.dto;

import com.umc.todayter.domain.auth.service.AuthTokenResult;
import com.umc.todayter.domain.onboarding.enums.OnboardingStep;

public record AppleLoginResult(
        Long memberId,
        boolean newMember,
        AuthTokenResult tokenResult,
        OnboardingStep onboardingStep
) {
}
