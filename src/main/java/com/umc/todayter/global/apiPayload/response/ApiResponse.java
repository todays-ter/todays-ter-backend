package com.umc.todayter.global.apiPayload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {
    private final Boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL) // result가 null이면 -> JSON 응답에서 result 필드 자체를 제외함
    private final T result;

    public static <T> ApiResponse<T> onSuccess(T result, BaseCode code) {
        return ApiResponse.<T> builder()
                .isSuccess(true)
                .code(code.getCode())
                .message(code.getMessage())
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> onFailure(T result, BaseCode code) {
        return ApiResponse.<T> builder()
                .isSuccess(false)
                .code(code.getCode())
                .message(code.getMessage())
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> onFailure(T result, BaseCode code, String message) { // 직접 메시지 지정
        return ApiResponse.<T> builder()
                .isSuccess(false)
                .code(code.getCode())
                .message(message)
                .result(result)
                .build();
    }
}
