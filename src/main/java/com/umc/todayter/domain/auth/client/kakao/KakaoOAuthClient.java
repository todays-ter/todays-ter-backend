package com.umc.todayter.domain.auth.client.kakao;

import com.umc.todayter.domain.auth.client.kakao.dto.KakaoTokenResponse;
import com.umc.todayter.domain.auth.client.kakao.dto.KakaoUserResponse;
import com.umc.todayter.domain.auth.dto.KakaoUserInfo;
import com.umc.todayter.domain.auth.exception.AuthErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.config.properties.KakaoOAuthProperties;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 인가 코드
 * -> 카카오 Access Token 발급
 * -> 카카오 사용자 정보 조회
 * -> 오늘의 터 공통 DTO로 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String BEARER_PREFIX = "Bearer ";

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final KakaoOAuthProperties properties;

    public KakaoUserInfo login(String authorizationCode) {
        String kakaoAccessToken = getAccessToken(authorizationCode);

        return getUserInfo(kakaoAccessToken);
    }

    private String getAccessToken(String authorizationCode) {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

            form.add("grant_type", AUTHORIZATION_CODE);
            form.add("client_id", properties.clientId());
            form.add("redirect_uri", properties.redirectUri());
            form.add("code", authorizationCode);

            if (StringUtils.hasText(properties.clientSecret())) {
                form.add("client_secret", properties.clientSecret());
            }

            KakaoTokenResponse response = kakaoAuthClient.issueToken(form);

            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new CustomException(AuthErrorCode.KAKAO_TOKEN_API_FAILED);
            }

            return response.accessToken();
        } catch (FeignException e) {
            log.warn(
                    "Kakao token request failed. status={}, response={}",
                    e.status(),
                    e.contentUTF8()
            );

            if (e.status() == 400) {
                throw new CustomException(AuthErrorCode.KAKAO_AUTHORIZATION_CODE_INVALID);
            }

            throw new CustomException(AuthErrorCode.KAKAO_TOKEN_API_FAILED);
        }
    }

    private KakaoUserInfo getUserInfo(String kakaoAccessToken) {
        try {
            KakaoUserResponse response = kakaoApiClient.getUserInformation(BEARER_PREFIX + kakaoAccessToken);

            if (response == null || response.id() == null) {
                throw new CustomException(AuthErrorCode.KAKAO_USER_ID_MISSING);
            }

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
        } catch (FeignException e) {
            log.warn(
                    "Kakao user info request failed. status={}, response={}",
                    e.status(),
                    e.contentUTF8()
            );

            throw new CustomException(AuthErrorCode.KAKAO_USER_INFO_FAILED);
        }
    }
}
