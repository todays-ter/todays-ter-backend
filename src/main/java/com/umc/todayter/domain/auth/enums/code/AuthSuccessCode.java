package com.umc.todayter.domain.auth.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseCode {

    DEV_TOKEN_ISSUED(HttpStatus.OK, "COMMON200", "개발용 토큰이 발급되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
