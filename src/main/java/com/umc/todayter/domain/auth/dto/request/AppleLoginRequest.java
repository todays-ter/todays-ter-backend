package com.umc.todayter.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppleLoginRequest(
        @NotBlank(message = "애플 인가 코드는 필수입니다.")
        String authorizationCode,

        @NotBlank(message = "애플 Identity Token은 필수입니다.")
        String identityToken,

        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {
}
