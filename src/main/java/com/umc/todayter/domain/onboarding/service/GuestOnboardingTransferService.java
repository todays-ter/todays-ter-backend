package com.umc.todayter.domain.onboarding.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.GuestSessionStatus;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestOnboardingTransferService {

    private final GuestSessionRepository guestSessionRepository;
    private final OnboardingRepository onboardingRepository;

    @Transactional
    public void transferIfPresent(String guestId, Member member) {
        if (guestId == null || guestId.isBlank()) {
            return;
        }

        GuestSession guestSession = guestSessionRepository
                .findByGuestId(guestId)
                .orElse(null);

        if (guestSession == null || !isTransferable(guestSession)) {
            return;
        }

        Onboarding guestOnboarding = onboardingRepository
                .findByGuestSessionId(guestSession.getId())
                .orElse(null);

        if (guestOnboarding == null) {
            return;
        }

        // 사용자에게 기존 온보딩 정보가 있으면 덮어쓰지 않음
        if (onboardingRepository.existsByMemberId(member.getId())) {
            guestSession.convertTo(member.getId());

            return;
        }

        guestOnboarding.transferToMember(member.getId());
        guestSession.convertTo(member.getId());
    }

    private boolean isTransferable(GuestSession guestSession) {
        return guestSession.getStatus() == GuestSessionStatus.ACTIVE && guestSession.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
