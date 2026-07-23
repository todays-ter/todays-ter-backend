package com.umc.todayter.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DevTokenRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
