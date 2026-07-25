package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.client.kakao.KakaoOAuthClient;
import com.umc.todayter.domain.auth.dto.KakaoLoginResult;
import com.umc.todayter.domain.auth.dto.KakaoUserInfo;
import com.umc.todayter.domain.auth.dto.SocialMemberResult;
import com.umc.todayter.domain.member.entity.Member;
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

    @Transactional
    public KakaoLoginResult login(String authorizationCode) {
        KakaoUserInfo userInfo = kakaoOAuthClient.login(authorizationCode);

        SocialMemberResult memberResult = kakaoMemberService.findOrCreate(userInfo);

        Member member = memberResult.member();

        AuthTokenResult tokenResult = authTokenService.issueTokens(member);

        return new KakaoLoginResult(
                member.getId(),
                memberResult.newMember(),
                tokenResult
        );
    }
}
