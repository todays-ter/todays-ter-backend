package com.umc.todayter.domain.auth.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            @JsonProperty("profile_needs_agreement")
            Boolean profileNeedsAgreement,

            @JsonProperty("profile")
            Profile profile,

            @JsonProperty("email_needs_agreement")
            Boolean emailNeedsAgreement,

            @JsonProperty("is_email_valid")
            Boolean isEmailValid,

            @JsonProperty("is_email_verified")
            Boolean isEmailVerified,

            @JsonProperty("email")
            String email
    ) {
    }

    public record Profile(
            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("is_default_image")
            Boolean isDefaultImage
    ) {
    }
}
