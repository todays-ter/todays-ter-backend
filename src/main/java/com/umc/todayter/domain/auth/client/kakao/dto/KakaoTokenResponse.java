package com.umc.todayter.domain.auth.client.kakao.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

// 카카오 응답은 snake_case로 옴
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoTokenResponse(
        String tokenType,
        String accessToken,
        Integer expiresIn,
        String refreshToken, // 카카오 API 토큰 갱신용
        Integer refreshTokenExpiresIn,
        String scope
) {
}
