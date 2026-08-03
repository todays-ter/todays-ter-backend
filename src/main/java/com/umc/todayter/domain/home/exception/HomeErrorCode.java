package com.umc.todayter.domain.home.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HomeErrorCode implements BaseCode {
    INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "HOME400_1", "위도와 경도 입력값이 올바르지 않습니다."),
    FORTUNE_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "HOME404_2", "조회 가능한 사주 리포트가 없습니다."),
    FORTUNE_REPORT_PROCESSING(HttpStatus.CONFLICT, "HOME409_1", "사주 리포트가 아직 준비되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
