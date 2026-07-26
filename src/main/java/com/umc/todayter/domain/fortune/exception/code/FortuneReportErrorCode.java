package com.umc.todayter.domain.fortune.exception.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FortuneReportErrorCode implements BaseCode {
    ONBOARDING_NOT_FOUND(HttpStatus.BAD_REQUEST, "FORTUNE400_1", "리포트 생성에 필요한 온보딩 정보가 없습니다."),
    SAJU_INFORMATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "FORTUNE400_2", "생년월일과 출생 시간 정보를 먼저 입력해 주세요."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "FORTUNE404_1", "리포트를 찾을 수 없습니다."),
    REPORT_ALREADY_PROCESSING(HttpStatus.CONFLICT, "FORTUNE409_1", "이미 생성 중인 리포트가 있습니다."),
    REPORT_NOT_RETRYABLE(HttpStatus.CONFLICT, "FORTUNE409_2", "실패한 리포트만 재시도할 수 있습니다."),
    RETRY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "FORTUNE409_3", "리포트 생성 재시도 횟수를 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
