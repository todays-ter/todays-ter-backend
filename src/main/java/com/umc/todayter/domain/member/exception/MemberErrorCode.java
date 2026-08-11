package com.umc.todayter.domain.member.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseCode {

    DUPLICATE_CONCERN_TYPE(HttpStatus.BAD_REQUEST, "MEMBER400", "중복된 고민 유형이 포함되어 있습니다."),

    MEMBER_INACTIVE(HttpStatus.FORBIDDEN, "MEMBER403", "접근이 불가한 사용자입니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "사용자를 찾을 수 없습니다."),
    SAJU_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_2", "저장된 사주 정보를 찾을 수 없습니다."),
    ONBOARDING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_3", "회원에게 연결된 온보딩 정보를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
