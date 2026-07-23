package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.dto.response.TokenResponse;

public record AuthTokenResult(
        TokenResponse response,
        String refreshToken,
        long refreshMaxAgeSeconds
) {
}
