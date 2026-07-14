package com.umc.todayter.global.apiPayload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseCode {
    // COMMON 2XX
    OK(HttpStatus.OK, "COMMON200", "성공적으로 요청을 처리했습니다."),
    CREATED(HttpStatus.CREATED, "COMMON201", "리소스가 생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, "COMMON202", "요청을 처리 중입니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON204", "성공적으로 요청을 처리했지만, 콘텐츠가 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
