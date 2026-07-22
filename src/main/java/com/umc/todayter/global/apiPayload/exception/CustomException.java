package com.umc.todayter.global.apiPayload.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final BaseCode errorCode;

    public CustomException(BaseCode errorCode) { // 기본 메시지 사용
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(BaseCode errorCode, String message) { // 직접 메시지 지정
        super(message);
        this.errorCode = errorCode;
    }
}
