package com.umc.todayter.domain.place.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PlaceErrorCode implements BaseCode {

    MISSING_TYPE_PARAMETER(HttpStatus.BAD_REQUEST, "PLACE400_1", "type 파라미터가 필요합니다."),
    INVALID_TYPE_PARAMETER(HttpStatus.BAD_REQUEST, "PLACE400_2", "type 값이 올바르지 않습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE404_1", "존재하지 않는 장소입니다."),
    PLACE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE404_2", "google에서 사진을 제공하지 않습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
