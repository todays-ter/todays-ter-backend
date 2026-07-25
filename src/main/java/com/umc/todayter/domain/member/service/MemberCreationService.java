package com.umc.todayter.domain.member.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 별도 트랜잭션으로 실행
 * 중복 INSERT가 실패해도 바깥 조회 로직은 정상적으로 계속 실행됨
 */
@Service
@RequiredArgsConstructor
public class MemberCreationService {

    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createDeveloperMember(String email, String nickname) {
        Member member = memberRepository.saveAndFlush(
                Member.create(email, nickname)
        );

        return member.getId();
    }
}
