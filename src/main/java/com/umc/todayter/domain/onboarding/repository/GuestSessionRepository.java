package com.umc.todayter.domain.onboarding.repository;

import com.umc.todayter.domain.onboarding.entity.GuestSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface GuestSessionRepository extends JpaRepository<GuestSession, Long> {
    Optional<GuestSession> findByGuestId(String guestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GuestSession> findForUpdateByGuestId(String guestId);
}
