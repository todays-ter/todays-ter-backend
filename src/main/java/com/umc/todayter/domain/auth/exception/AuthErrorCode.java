package com.umc.todayter.domain.auth.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

    KAKAO_TOKEN_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "AUTH400", "카카오 토큰 요청 정보가 올바르지 않습니다."),
    APPLE_TOKEN_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "AUTH400_2", "애플 토큰 요청 정보가 올바르지 않습니다."),

    KAKAO_AUTHORIZATION_CODE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH401_1", "카카오 인가 코드가 유효하지 않거나 만료되었습니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH401_2", "Refresh Token이 존재하지 않습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH401_3", "유효하지 않거나 만료된 Refresh Token입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH401_4", "저장된 Refresh Token과 일치하지 않습니다."),
    KAKAO_REDIRECT_URI_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH401_5", "카카오 Redirect URI가 일치하지 않습니다."),
    KAKAO_CLIENT_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_6", "카카오 클라이언트 인증에 실패했습니다."),
    APPLE_AUTHORIZATION_CODE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH401_7", "애플 인가 코드가 유효하지 않거나 만료되었습니다."),
    APPLE_CLIENT_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_8", "애플 클라이언트 인증에 실패했습니다."),

    INVALID_ORIGIN(HttpStatus.FORBIDDEN, "AUTH403", "허용되지 않은 출처의 요청입니다."),

    APPLE_CLIENT_SECRET_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH500", "Apple Client Secret 생성에 실패했습니다."),

    KAKAO_TOKEN_API_FAILED(HttpStatus.BAD_GATEWAY, "AUTH502_1", "카카오 인증 서버와 통신하지 못했습니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "AUTH502_2", "카카오 사용자 정보를 조회하지 못했습니다."),
    KAKAO_USER_ID_MISSING(HttpStatus.BAD_GATEWAY, "AUTH502_3", "카카오 사용자 식별 정보를 확인할 수 없습니다."),
    APPLE_TOKEN_API_FAILED(HttpStatus.BAD_GATEWAY, "AUTH502_4", "애플 인증 서버와 통신하지 못했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
