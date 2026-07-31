package com.umc.todayter.domain.auth.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseCode {

    DEV_TOKEN_ISSUED(HttpStatus.OK, "COMMON200", "개발용 토큰이 발급되었습니다."),
    KAKAO_LOGIN_SUCCESS(HttpStatus.OK, "AUTH200_1", "카카오 로그인에 성공했습니다."),
    TOKEN_REISSUED(HttpStatus.OK, "AUTH200_2", "토큰이 재발급되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH200_3", "로그아웃되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
