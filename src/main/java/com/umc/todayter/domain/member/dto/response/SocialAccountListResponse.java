package com.umc.todayter.domain.member.dto.response;

import com.umc.todayter.domain.member.entity.SocialAccount;

import java.util.List;

public record SocialAccountListResponse(
        List<SocialAccountResponse> socialAccounts
) {
    public static SocialAccountListResponse from(
            List<SocialAccount> socialAccounts
    ) {
        List<SocialAccountResponse> responses = socialAccounts.stream()
                .map(SocialAccountResponse::from)
                .toList();

        return new SocialAccountListResponse(responses);
    }
}
