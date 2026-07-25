package com.umc.todayter.domain.auth.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken, // 카카오 API 토큰 갱신용

        @JsonProperty("refresh_token_expires_in")
        Integer refreshTokenExpiresIn,

        @JsonProperty("scope")
        String scope
) {
}
