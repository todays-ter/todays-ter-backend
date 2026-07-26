package com.umc.todayter.domain.record.exception;

import com.umc.todayter.global.apiPayload.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecordErrorCode implements BaseCode {

    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECORD404_1", "장소를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECORD404_2", "존재하지 않는 이미지입니다."),
    IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RECORD403_1", "접근 권한이 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "RECORD409_1", "이미 작성된 후기가 있습니다."),
    IMAGE_ALREADY_USED(HttpStatus.CONFLICT, "RECORD409_2", "이미 사용된 이미지입니다."),
    EMPTY_IMAGE_FILE(HttpStatus.BAD_REQUEST, "RECORD400_1", "업로드할 이미지가 없습니다."),
    INVALID_IMAGE_FILE_TYPE(HttpStatus.BAD_REQUEST, "RECORD400_2", "지원하지 않는 파일 형식입니다."),
    IMAGE_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "RECORD400_3", "파일 용량이 너무 큽니다."),
    TOO_MANY_IMAGE_FILES(HttpStatus.BAD_REQUEST, "RECORD400_4", "이미지는 최대 5장까지 업로드 가능합니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RECORD500_1", "이미지 업로드에 실패했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
