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
            KakaoTokenResponse response = kakaoAuthClient.issueToken(
                    AUTHORIZATION_CODE,
                    properties.clientId(),
                    properties.redirectUri(),
                    authorizationCode,
                    properties.clientSecret()
            );

            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new CustomException(AuthErrorCode.KAKAO_TOKEN_ISSUE_FAILED);
            }

            return response.accessToken();
        } catch (FeignException e) {
            log.warn(
                    "Kakao token request failed. status={}, response={}",
                    e.status(),
                    e.contentUTF8()
            );

            throw new CustomException(AuthErrorCode.KAKAO_TOKEN_ISSUE_FAILED);
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
        } catch (CustomException e) {
            throw e;
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
