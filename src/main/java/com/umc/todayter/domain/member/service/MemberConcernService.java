package com.umc.todayter.domain.member.service;

import com.umc.todayter.domain.member.dto.response.MemberConcernResponse;
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
public class MemberConcernService {

    private final OnboardingRepository onboardingRepository;
    private final MemberService memberService;

    public MemberConcernResponse getConcerns(Long memberId) {
        memberService.getActiveMember(memberId);

        Onboarding onboarding = onboardingRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.ONBOARDING_NOT_FOUND));

        return MemberConcernResponse.from(onboarding);
    }
}
