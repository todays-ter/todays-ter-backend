package com.umc.todayter.domain.fortune.repository;

import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface FortuneReportRepository extends JpaRepository<FortuneReport, Long> {
    Optional<FortuneReport> findFirstByMemberIdAndStatusOrderByIdDesc(Long memberId, FortuneReportStatus status);
    Optional<FortuneReport> findFirstByGuestSessionIdAndStatusOrderByIdDesc(Long guestSessionId, FortuneReportStatus status);
    Optional<FortuneReport> findFirstByMemberIdOrderByIdDesc(Long memberId);

    Optional<FortuneReport> findByIdAndMemberId(Long id, Long memberId);
    Optional<FortuneReport> findByIdAndGuestSessionId(Long id, Long guestSessionId);
    Optional<FortuneReport> findByShareToken(String shareToken);
    boolean existsByShareToken(String shareToken);
    List<FortuneReport> findAllByGuestSessionId(Long guestSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from FortuneReport r where r.id = :id and r.memberId = :memberId")
    Optional<FortuneReport> findOwnedByIdForUpdate(@Param("id") Long id, @Param("memberId") Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from FortuneReport r where r.id = :id and r.guestSessionId = :guestSessionId")
    Optional<FortuneReport> findGuestOwnedByIdForUpdate(
            @Param("id") Long id,
            @Param("guestSessionId") Long guestSessionId
    );
}
