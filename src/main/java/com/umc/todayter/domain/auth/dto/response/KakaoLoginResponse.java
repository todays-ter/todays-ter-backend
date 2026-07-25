package com.umc.todayter.domain.auth.dto.response;

public record KakaoLoginResponse(
        Long memberId,
        String accessToken,
        boolean isNewMember
) {
}
