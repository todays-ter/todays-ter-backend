package com.umc.todayter.domain.member.service;

import com.umc.todayter.domain.member.dto.response.MemberSajuResponse;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSajuService {

    private final OnboardingRepository onboardingRepository;
    private final MemberService memberService;

    public MemberSajuResponse getSaju(Long memberId) {
        // 탈퇴 회원 또는 존재하지 않는 회원 접근 방지
        memberService.getActiveMember(memberId);

        Onboarding onboarding = onboardingRepository
                .findByMemberId(memberId)
                .filter(Onboarding::hasSajuInformation)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SAJU_NOT_FOUND));

        return MemberSajuResponse.from(onboarding);
    }
}
