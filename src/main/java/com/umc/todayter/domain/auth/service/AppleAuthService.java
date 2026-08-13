package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.client.apple.AppleIdentityTokenValidator;
import com.umc.todayter.domain.auth.client.apple.AppleOAuthClient;
import com.umc.todayter.domain.auth.dto.AppleLoginResult;
import com.umc.todayter.domain.auth.dto.AppleUserInfo;
import com.umc.todayter.domain.auth.dto.SocialMemberResult;
import com.umc.todayter.domain.fortune.service.GuestFortuneReportTransferService;
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
public class AppleAuthService {

    private final AppleOAuthClient appleOAuthClient;
    private final AppleIdentityTokenValidator appleIdentityTokenValidator;
    private final AppleMemberService appleMemberService;
    private final AuthTokenService authTokenService;
    private final GuestOnboardingTransferService guestOnboardingTransferService;
    private final GuestFortuneReportTransferService guestFortuneReportTransferService;
    private final OnboardingRepository onboardingRepository;

    @Transactional
    public AppleLoginResult login(
            String authorizationCode,
            String identityToken,
            String nickname,
            String guestId
    ) {
        // 애플 Authorization Code를 애플 Token으로 교환
        appleOAuthClient.issueToken(authorizationCode);

        // 애플 Identity Token 검증 후 사용자 식별 정보 조회
        AppleUserInfo userInfo = appleIdentityTokenValidator.validate(identityToken);

        // 기존 애플 소셜 계정을 조회하거나 신규 회원 생성
        SocialMemberResult memberResult =
                appleMemberService.findOrCreate(userInfo, nickname);

        Member member = memberResult.member();

        // 비회원 상태에서 생성한 운세 리포트가 있으면 회원에게 이전
        guestFortuneReportTransferService.transferIfPresent(guestId, member);

        // 비회원 온보딩 정보가 존재하면 회원에게 이전
        guestOnboardingTransferService.transferIfPresent(guestId, member);

        // 서비스 Access Token / Refresh Token 발급
        AuthTokenResult tokenResult = authTokenService.issueTokens(member);

        // 로그인 이후 현재 온보딩 진행 상태 반환
        OnboardingStep onboardingStep = onboardingRepository
                .findByMemberId(member.getId())
                .map(Onboarding::getOnboardingStep)
                .orElse(OnboardingStep.STARTED);

        return new AppleLoginResult(
                member.getId(),
                memberResult.newMember(),
                tokenResult,
                onboardingStep
        );
    }
}
