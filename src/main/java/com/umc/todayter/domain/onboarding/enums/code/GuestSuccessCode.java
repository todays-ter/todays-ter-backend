package com.umc.todayter.domain.onboarding.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GuestSuccessCode implements BaseCode {

    GUEST_SESSION_STATUS_RETRIEVED(HttpStatus.OK, "GUEST200", "게스트 세션 상태를 조회했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
