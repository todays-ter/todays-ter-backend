package com.umc.todayter.domain.onboarding.service;

import com.umc.todayter.domain.onboarding.dto.request.GuestConcernRequest;
import com.umc.todayter.domain.onboarding.dto.request.GuestSajuRequest;
import com.umc.todayter.domain.onboarding.dto.response.GuestConcernResponse;
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
import java.util.HashSet;
import java.util.List;

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

    @Transactional
    public GuestConcernResponse saveConcerns(String guestId, GuestConcernRequest request) {
        // 비회원 세션 유효성 검사
        GuestSession guestSession = getValidGuestSession(guestId);

        Onboarding onboarding = onboardingRepository
                .findByGuestSessionId(guestSession.getId())
                .orElseThrow(() -> new CustomException(OnboardingErrorCode.ONBOARDING_NOT_FOUND));

        // 사주 입력이 완료되지 않았다면 고민 저장 X
        if (!onboarding.hasSajuInformation()) {
            throw new CustomException(OnboardingErrorCode.SAJU_INFORMATION_NOT_FOUND);
        }

        // 요청 목록에 중복된 고민 유형이 있는지 검사
        if (new HashSet<>(request.concernTypes()).size() != request.concernTypes().size()) {
            throw new CustomException(OnboardingErrorCode.DUPLICATE_CONCERN_TYPE);
        }

        onboarding.updateConcerns(request.concernTypes());

        return new GuestConcernResponse(
                List.copyOf(onboarding.getConcernTypes()),
                onboarding.getOnboardingStep()
        );
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
