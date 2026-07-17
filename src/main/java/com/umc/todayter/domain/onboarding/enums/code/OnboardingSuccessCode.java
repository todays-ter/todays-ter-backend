package com.umc.todayter.domain.onboarding.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OnboardingSuccessCode implements BaseCode {

    SAJU_SAVED(HttpStatus.OK, "ONBOARDING200", "사주 정보가 저장되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
