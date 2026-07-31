package com.umc.todayter.domain.member.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseCode {

    SAJU_RETRIEVED(HttpStatus.OK, "MEMBER200_1", "사주 정보를 조회했습니다."),
    MEMBER_WITHDRAWN(HttpStatus.OK, "MEMBER200_2", "회원 탈퇴가 완료되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
