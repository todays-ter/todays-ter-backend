package com.umc.todayter.domain.onboarding.repository;

import com.umc.todayter.domain.onboarding.entity.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {
    Optional<Onboarding> findByGuestSessionId(Long guestSessionId);
}
