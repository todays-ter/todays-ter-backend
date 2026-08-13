package com.umc.todayter.domain.auth.dto.response;

import com.umc.todayter.domain.onboarding.enums.OnboardingStep;

public record AppleLoginResponse(
        Long memberId,
        String accessToken,
        boolean isNewMember,
        OnboardingStep onboardingStep
) {
}
