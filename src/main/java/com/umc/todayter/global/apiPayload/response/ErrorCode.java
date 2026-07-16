package com.umc.todayter.global.apiPayload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {
    // COMMON 4XX (클라이언트 오류)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON400_1", "입력값이 올바르지 않습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증되지 않은 요청입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "접근이 금지되었습니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청하신 리소스를 찾을 수 없습니다."),

    CONFLICT(HttpStatus.CONFLICT, "COMMON409", "서버 상태와 충돌하는 요청입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON409_1", "이미 존재하는 데이터입니다."),

    // COMMON 5XX (서버 오류)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
