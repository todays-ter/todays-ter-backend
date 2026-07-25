package com.umc.todayter.domain.auth.client.kakao;

import com.umc.todayter.domain.auth.client.kakao.dto.KakaoTokenResponse;
import com.umc.todayter.global.config.properties.KakaoOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private static final String AUTHORIZATION_CODE = "authorization_code";

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoOAuthProperties properties;

    public String getAccessToken(String authorizationCode) {
        KakaoTokenResponse response = kakaoAuthClient.issueToken(
                AUTHORIZATION_CODE,
                properties.clientId(),
                properties.redirectUri(),
                authorizationCode,
                properties.clientSecret()
        );

        return response.accessToken();
    }
}
