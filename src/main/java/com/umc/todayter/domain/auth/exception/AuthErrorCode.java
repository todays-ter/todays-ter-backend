package com.umc.todayter.domain.auth.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

    SOCIAL_EMAIL_ALREADY_USE(HttpStatus.CONFLICT, "AUTH409", "해당 이메일로 가입된 다른 계정이 존재합니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
