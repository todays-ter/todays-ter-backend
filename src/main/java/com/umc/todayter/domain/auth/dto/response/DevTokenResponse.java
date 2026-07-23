package com.umc.todayter.domain.auth.dto.response;

public record DevTokenResponse(
        Long memberId,
        String accessToken
) {
}
