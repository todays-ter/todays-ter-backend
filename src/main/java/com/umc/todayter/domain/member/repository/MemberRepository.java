package com.umc.todayter.domain.member.repository;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByIdAndStatus(Long memberId, MemberStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :memberId and m.status = :status")
    Optional<Member> findByIdAndStatusForUpdate(
            @Param("memberId") Long memberId,
            @Param("status") MemberStatus status
    );

    Optional<Member> findFirstByEmailAndStatusOrderByIdAsc(String email, MemberStatus status);
}
