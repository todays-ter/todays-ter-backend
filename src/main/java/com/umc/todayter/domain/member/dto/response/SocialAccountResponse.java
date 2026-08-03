package com.umc.todayter.domain.member.dto.response;

import com.umc.todayter.domain.member.entity.SocialAccount;
import com.umc.todayter.domain.member.enums.SocialProvider;

public record SocialAccountResponse(
        SocialProvider provider,
        String email
) {
    public static SocialAccountResponse from(
            SocialAccount socialAccount
    ) {
        return new SocialAccountResponse(
                socialAccount.getProvider(),
                socialAccount.getProviderEmail()
        );
    }
}
