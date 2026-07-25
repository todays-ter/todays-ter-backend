package com.umc.todayter.domain.auth.dto;

import com.umc.todayter.domain.auth.service.AuthTokenResult;

public record KakaoLoginResult(
        Long memberId,
        boolean newMember,
        AuthTokenResult tokenResult
) {
}
