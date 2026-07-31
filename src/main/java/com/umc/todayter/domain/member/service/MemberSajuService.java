package com.umc.todayter.domain.member.service;

import com.umc.todayter.domain.member.dto.request.MemberSajuUpdateRequest;
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
        memberService.getActiveMember(memberId);
        Onboarding onboarding = getMemberSajuOnboarding(memberId);

        return MemberSajuResponse.from(onboarding);
    }

    @Transactional
    public MemberSajuResponse updateSaju(Long memberId, MemberSajuUpdateRequest request) {
        memberService.getActiveMember(memberId);
        Onboarding onboarding = getMemberSajuOnboarding(memberId);

        onboarding.updateSaju(
                request.calendarType(),
                request.birthDate(),
                request.birthTime(),
                request.birthTimeUnknown()
        );

        return MemberSajuResponse.from(onboarding);
    }

    private Onboarding getMemberSajuOnboarding(Long memberId) {
        return onboardingRepository
                .findByMemberId(memberId)
                .filter(Onboarding::hasSajuInformation)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SAJU_NOT_FOUND));
    }
}
