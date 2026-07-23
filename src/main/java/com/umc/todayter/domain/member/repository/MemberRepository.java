package com.umc.todayter.domain.member.repository;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByIdAndStatus(Long memberId, MemberStatus status);
    Optional<Member> findByEmailAndStatus(String email, MemberStatus status);
}
