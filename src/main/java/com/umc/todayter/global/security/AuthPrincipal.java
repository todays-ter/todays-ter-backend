package com.umc.todayter.global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthPrincipal {
    private final Long memberId;
}
