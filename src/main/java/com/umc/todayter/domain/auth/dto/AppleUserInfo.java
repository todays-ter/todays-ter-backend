package com.umc.todayter.domain.auth.dto;

public record AppleUserInfo(
        String providerUserId, // Apple Identity Token의 sub
        String email           // Apple Identity Token의 email
) {
}
