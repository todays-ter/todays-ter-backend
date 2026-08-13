package com.umc.todayter.domain.auth.client.apple;

import com.umc.todayter.domain.auth.client.apple.dto.AppleTokenResponse;
import com.umc.todayter.domain.auth.exception.AuthErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.config.properties.AppleOAuthProperties;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuthClient {

    private static final String AUTHORIZATION_CODE = "authorization_code";

    private final AppleAuthClient appleAuthClient;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final AppleOAuthProperties properties;

    public AppleTokenResponse issueToken(String authorizationCode) {

        try {
            String clientSecret = appleClientSecretGenerator.generate();

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

            form.add("client_id", properties.clientId());
            form.add("client_secret", clientSecret);
            form.add("code", authorizationCode);
            form.add("grant_type", AUTHORIZATION_CODE);
            form.add("redirect_uri", properties.redirectUri());

            AppleTokenResponse response = appleAuthClient.issueToken(form);

            if (response == null || !StringUtils.hasText(response.idToken())) {
                throw new CustomException(AuthErrorCode.APPLE_TOKEN_API_FAILED);
            }

            return response;

        } catch (FeignException e) {

            log.warn(
                    "Apple token request failed. status={}, response={}",
                    e.status(),
                    e.contentUTF8()
            );

            throw mapAppleTokenException(e);
        }
    }

    private CustomException mapAppleTokenException(FeignException e) {

        String response = e.contentUTF8();

        if (e.status() == 400) {

            if (containsIgnoreCase(
                    response,
                    "invalid_grant"
            )) {
                return new CustomException(AuthErrorCode.APPLE_AUTHORIZATION_CODE_INVALID);
            }

            if (containsIgnoreCase(
                    response,
                    "invalid_client"
            )) {
                return new CustomException(AuthErrorCode.APPLE_CLIENT_AUTH_FAILED);
            }

            if (containsIgnoreCase(
                    response,
                    "invalid_request"
            )) {
                return new CustomException(AuthErrorCode.APPLE_TOKEN_REQUEST_INVALID);
            }
        }

        return new CustomException(AuthErrorCode.APPLE_TOKEN_API_FAILED);
    }

    private boolean containsIgnoreCase(String source, String... keywords) {

        if (!StringUtils.hasText(source)) {
            return false;
        }

        String normalized = source.toLowerCase();

        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
