package com.umc.todayter.domain.record.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecordErrorCode implements BaseCode {

    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECORD404_1", "장소를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
