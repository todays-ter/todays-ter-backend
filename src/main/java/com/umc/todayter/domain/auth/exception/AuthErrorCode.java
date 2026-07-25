package com.umc.todayter.domain.auth.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

    KAKAO_AUTHORIZATION_CODE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH401", "카카오 인가 코드가 유효하지 않거나 만료되었습니다."),

    KAKAO_TOKEN_API_FAILED(HttpStatus.BAD_GATEWAY, "AUTH502_1", "카카오 인증 서버와 통신하지 못했습니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "AUTH502_2", "카카오 사용자 정보를 조회하지 못했습니다."),
    KAKAO_USER_ID_MISSING(HttpStatus.BAD_GATEWAY, "AUTH502_3", "카카오 사용자 식별 정보를 확인할 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
