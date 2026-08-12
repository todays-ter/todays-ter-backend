package com.umc.todayter.domain.member.service;

import com.umc.todayter.domain.member.dto.request.MemberConcernUpdateRequest;
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

    @Transactional
    public MemberConcernResponse updateConcerns(Long memberId, MemberConcernUpdateRequest request) {
        memberService.getActiveMember(memberId);

        Onboarding onboarding = onboardingRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.ONBOARDING_NOT_FOUND));

        if (request.concernTypes().stream().distinct().count() != request.concernTypes().size()) {
            throw new CustomException(MemberErrorCode.DUPLICATE_CONCERN_TYPE);
        }

        onboarding.updateConcerns(request.concernTypes());

        return MemberConcernResponse.from(onboarding);
    }
}
