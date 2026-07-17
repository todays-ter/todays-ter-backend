package com.umc.todayter.domain.onboarding.service;

import com.umc.todayter.domain.onboarding.dto.request.GuestSajuRequest;
import com.umc.todayter.domain.onboarding.dto.response.GuestSajuResponse;
import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.GuestSessionStatus;
import com.umc.todayter.domain.onboarding.exception.code.OnboardingErrorCode;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestOnboardingService {

    private final GuestSessionRepository guestSessionRepository;
    private final OnboardingRepository onboardingRepository;

    @Transactional
    public GuestSajuResponse saveSaju(String guestId, GuestSajuRequest request) {
        GuestSession guestSession = getValidGuestSession(guestId);

        Onboarding onboarding = onboardingRepository
                .findByGuestSessionId(guestSession.getId())
                .orElseGet(() -> onboardingRepository.save(Onboarding.createForGuest(guestSession)));

        onboarding.updateSaju(request);

        return new GuestSajuResponse(onboarding.getOnboardingStep());
    }

    private GuestSession getValidGuestSession(String guestId) {
        if (guestId == null || guestId.isBlank()) {
            throw new CustomException(OnboardingErrorCode.GUEST_COOKIE_REQUIRED);
        }

        GuestSession guestSession = guestSessionRepository
                .findByGuestId(guestId)
                .orElseThrow(() -> new CustomException(OnboardingErrorCode.GUEST_SESSION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        if (!guestSession.getExpiresAt().isAfter(now)) {
            throw new CustomException(OnboardingErrorCode.GUEST_SESSION_EXPIRED);
        }

        if (guestSession.getStatus() != GuestSessionStatus.ACTIVE) {
            throw new CustomException(OnboardingErrorCode.GUEST_SESSION_UNAVAILABLE);
        }

        return guestSession;
    }
}
