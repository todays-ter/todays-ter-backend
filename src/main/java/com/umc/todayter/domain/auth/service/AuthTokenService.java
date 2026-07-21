package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.dto.response.TokenResponse;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.global.security.jwt.JwtProvider;
import com.umc.todayter.global.security.jwt.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 토큰 발급 서비스
@Service
@RequiredArgsConstructor
@Transactional
public class AuthTokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenHasher refreshTokenHasher;

    // 회원 정보를 받아서 Access Token과 Refresh Token을 발급하는 메서드
    public AuthTokenResult issueTokens(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId());

        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

        member.updateRefreshToken(refreshTokenHash, jwtProvider.getRefreshExpiresAt());

        return new AuthTokenResult(
                new TokenResponse(accessToken),
                refreshToken,
                jwtProvider.getRefreshMaxAgeSeconds()
        );
    }
}
