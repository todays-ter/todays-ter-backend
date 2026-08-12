package com.umc.todayter.domain.onboarding.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GuestSuccessCode implements BaseCode {

    GUEST_SESSION_STATUS_RETRIEVED(HttpStatus.OK, "GUEST200_1", "게스트 세션 상태를 조회했습니다."),
    GUEST_SESSION_CONVERTED(HttpStatus.OK, "GUEST200_2", "게스트 온보딩 정보가 회원 계정으로 이전되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
