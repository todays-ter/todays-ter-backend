package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.client.kakao.KakaoOAuthClient;
import com.umc.todayter.domain.auth.dto.KakaoLoginResult;
import com.umc.todayter.domain.auth.dto.KakaoUserInfo;
import com.umc.todayter.domain.auth.dto.SocialMemberResult;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.OnboardingStep;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import com.umc.todayter.domain.onboarding.service.GuestOnboardingTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoMemberService kakaoMemberService;
    private final AuthTokenService authTokenService;
    private final GuestOnboardingTransferService guestOnboardingTransferService;
    private final OnboardingRepository onboardingRepository;

    @Transactional
    public KakaoLoginResult login(String authorizationCode, String guestId) {
        KakaoUserInfo userInfo = kakaoOAuthClient.login(authorizationCode);

        SocialMemberResult memberResult = kakaoMemberService.findOrCreate(userInfo);

        Member member = memberResult.member();

        guestOnboardingTransferService.transferIfPresent(guestId, member);

        AuthTokenResult tokenResult = authTokenService.issueTokens(member);

        OnboardingStep onboardingStep = onboardingRepository
                .findByMemberId(member.getId())
                .map(Onboarding::getOnboardingStep)
                .orElse(OnboardingStep.STARTED);

        return new KakaoLoginResult(
                member.getId(),
                memberResult.newMember(),
                tokenResult,
                onboardingStep
        );
    }
}
