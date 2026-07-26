package com.umc.todayter.domain.auth.dto.response;

import com.umc.todayter.domain.onboarding.enums.OnboardingStep;

public record KakaoLoginResponse(
        Long memberId,
        String accessToken,
        boolean isNewMember,
        OnboardingStep onboardingStep
) {
}
