package com.umc.todayter.domain.auth.dto;

public record KakaoUserInfo(
        String providerUserId,
        String email,
        String nickname
) {
}
