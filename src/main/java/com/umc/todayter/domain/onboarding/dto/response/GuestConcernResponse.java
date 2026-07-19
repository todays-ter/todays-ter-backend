package com.umc.todayter.domain.onboarding.dto.response;

import com.umc.todayter.domain.onboarding.enums.ConcernType;
import com.umc.todayter.domain.onboarding.enums.OnboardingStep;

import java.util.List;

public record GuestConcernResponse(
        List<ConcernType> concernTypes,
        OnboardingStep onboardingStep
) {
}
