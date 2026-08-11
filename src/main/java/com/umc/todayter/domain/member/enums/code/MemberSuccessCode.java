package com.umc.todayter.domain.member.enums.code;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseCode {

    SAJU_RETRIEVED(HttpStatus.OK, "MEMBER200_1", "사주 정보를 조회했습니다."),
    CONCERNS_RETRIEVED(HttpStatus.OK, "MEMBER200_2", "고민 유형을 조회했습니다."),
    SAJU_UPDATED(HttpStatus.OK, "MEMBER200_3", "사주 정보가 수정되었습니다."),
    MEMBER_INFO_RETRIEVED(HttpStatus.OK, "MEMBER200_4", "회원 정보를 조회했습니다."),
    SOCIAL_ACCOUNTS_RETRIEVED(HttpStatus.OK, "MEMBER200_5", "연결된 소셜 계정을 조회했습니다."),
    MEMBER_WITHDRAWN(HttpStatus.OK, "MEMBER200_6", "회원 탈퇴가 완료되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
