package com.umc.todayter.domain.member.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberCreationService memberCreationService;

    public Member getActiveMember(Long memberId) {
        return memberRepository
                .findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    // 개발용 토큰 발급 API에서 사용
    public Member findOrCreateDeveloperMember(String email, String nickname) {
        return memberRepository
                .findFirstByEmailAndStatusOrderByIdAsc(email, MemberStatus.ACTIVE)
                .orElseGet(() -> {
                    Long memberId = memberCreationService.createDeveloperMember(
                            email,
                            nickname
                    );

                    return getActiveMember(memberId);
                });
    }
}
