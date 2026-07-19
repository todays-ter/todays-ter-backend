package com.umc.todayter.domain.onboarding.repository;

import com.umc.todayter.domain.onboarding.entity.GuestSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestSessionRepository extends JpaRepository<GuestSession, Long> {
    Optional<GuestSession> findByGuestId(String guestId);
}
