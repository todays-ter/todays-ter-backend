package com.umc.todayter.domain.fortune.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FortuneReportSuccessCode implements BaseCode {
    GENERATION_ACCEPTED(HttpStatus.ACCEPTED, "FORTUNE202_1", "리포트 생성을 시작했습니다."),
    STATUS_FOUND(HttpStatus.OK, "FORTUNE200_1", "리포트 생성 상태를 조회했습니다."),
    RETRY_ACCEPTED(HttpStatus.ACCEPTED, "FORTUNE202_2", "리포트 생성을 다시 시작했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
