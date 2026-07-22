package com.umc.todayter.domain.onboarding.service;

import com.umc.todayter.domain.onboarding.dto.response.GuestSessionResponse;
import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.GuestSessionStatus;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 쿠키가 있는가?
 * -> 있다면 DB에서 세션을 찾는다.
 * -> 세션이 살아 있다면(만료가 되지 않았다면) 그대로 쓰고
 * -> 만료되었거나 없으면 새로 만든다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestSessionService {

    private static final long EXPIRATION_DAYS = 30L;

    private final GuestSessionRepository guestSessionRepository;
    private final OnboardingRepository onboardingRepository;

    @Transactional
    public GuestSessionResult initialize(String cookieGuestId) {
        LocalDateTime now = LocalDateTime.now();

        // 쿠키가 있으면
        if (cookieGuestId != null && !cookieGuestId.isBlank()) {
            // 쿠키로 DB에서 세션을 찾음
            Optional<GuestSession> existingSession = guestSessionRepository.findByGuestId(cookieGuestId);

            if (existingSession.isPresent()) { // 세션을 찾았다면
                GuestSession guestSession = existingSession.get();

                // 유효한 세션이면 기존 온보딩 진행 상태 반환
                if (guestSession.isUsable(now)) {
                    Onboarding onboarding = findOrCreateOnboarding(guestSession);

                    return new GuestSessionResult(
                            guestSession.getGuestId(),
                            new GuestSessionResponse(onboarding.getOnboardingStep()),
                            false
                    );
                }

                // 만료 일시가 지났지만 상태가 ACTIVE라면 만료 상태(EXPIRED)로 변경 (만료 처리)
                if (guestSession.getStatus() == GuestSessionStatus.ACTIVE) {
                    guestSession.expire();
                }
            }
        }

        // 쿠키가 없거나 기존 세션을 사용할 수 없다면 새 세션 생성
        return createNewSession(now);
    }

    private GuestSessionResult createNewSession(LocalDateTime now) {
        String newGuestId = UUID.randomUUID().toString();

        GuestSession guestSession = GuestSession.create(newGuestId, now.plusDays(EXPIRATION_DAYS));

        guestSessionRepository.save(guestSession);

        // 새 비회원 세션과 함께 STARTED 상태의 온보딩 객체 생성
        Onboarding onboarding = onboardingRepository.save(Onboarding.createForGuest(guestSession));

        return new GuestSessionResult(
                newGuestId,
                new GuestSessionResponse(onboarding.getOnboardingStep()),
                true
        );
    }

    private Onboarding findOrCreateOnboarding(GuestSession guestSession) {
        // 기존 온보딩이 없으면 현재 비회원 세션에 연결하여 새로 생성
        return onboardingRepository
                .findByGuestSessionId(guestSession.getId())
                .orElseGet(() -> onboardingRepository.save(Onboarding.createForGuest(guestSession)));
    }

    public record GuestSessionResult(
            String guestId,
            GuestSessionResponse response,
            boolean cookieRefreshRequired
    ) {
    }
}
