package com.umc.todayter.domain.auth.client.kakao;

import com.umc.todayter.domain.auth.client.kakao.dto.KakaoTokenResponse;
import com.umc.todayter.domain.auth.client.kakao.dto.KakaoUserResponse;
import com.umc.todayter.domain.auth.dto.KakaoUserInfo;
import com.umc.todayter.global.config.properties.KakaoOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 인가 코드
 * -> 카카오 Access Token 발급
 * -> 카카오 사용자 정보 조회
 * -> 오늘의 터 공통 DTO로 변환
 */
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String BEARER_PREFIX = "Bearer ";

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final KakaoOAuthProperties properties;

    public String getAccessToken(String authorizationCode) {
        KakaoTokenResponse response = kakaoAuthClient.issueToken(
                AUTHORIZATION_CODE,
                properties.clientId(),
                properties.redirectUri(),
                authorizationCode,
                properties.clientSecret()
        );

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("카카오 Access Token 응답이 비어 있습니다.");
        }

        return response.accessToken();
    }

    public KakaoUserInfo getUserInfo(String kakaoAccessToken) {
        KakaoUserResponse response = kakaoApiClient.getUserInformation(BEARER_PREFIX + kakaoAccessToken);

        KakaoUserResponse.KakaoAccount account = response.kakaoAccount();

        String email = account != null ? account.email() : null;

        String nickname = account != null && account.profile() != null
                ? account.profile().nickname()
                : null;

        return new KakaoUserInfo(
                String.valueOf(response.id()),
                email,
                nickname
        );
    }

    public KakaoUserInfo login(String authorizationCode) {
        String kakaoAccessToken = getAccessToken(authorizationCode);

        return getUserInfo(kakaoAccessToken);
    }
}
