package com.umc.todayter.domain.onboarding.exception.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OnboardingErrorCode implements BaseCode {

    GUEST_COOKIE_REQUIRED(HttpStatus.UNAUTHORIZED, "GUEST401_1", "비회원 세션 쿠키가 필요합니다."),
    GUEST_SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "GUEST401_2", "유효하지 않은 비회원 세션입니다."),
    GUEST_SESSION_UNAVAILABLE(HttpStatus.UNAUTHORIZED, "GUEST401_3", "사용할 수 없는 비회원 세션입니다."),

    GUEST_SESSION_EXPIRED(HttpStatus.GONE, "GUEST410", "비회원 세션이 만료되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
