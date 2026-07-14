package com.umc.todayter.global.apiPayload.handler;

import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class}) // @RestController가 붙은 클래스에서 발생한 예외만 이 Advice가 처리함
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    // @RequestParam, @PathVariable 등 Bean Validation 실패 시
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e, WebRequest request) {
        String detail = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");

        ApiResponse<Object> body = ApiResponse.onFailure(detail, ErrorCode.INVALID_REQUEST);

        return handleExceptionInternal(
                e,
                body,
                new HttpHeaders(),
                ErrorCode.INVALID_REQUEST.getHttpStatus(),
                request
        );
    }

    // @Valid + @RequestBody DTO 검증 실패 시
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField(); // ex) field = "nickname"
            String message = Optional.ofNullable(fieldError.getDefaultMessage())
                    .orElse("유효하지 않은 값입니다."); // ex) message = "닉네임은 1자 이상이어야 합니다.", message가 null이면 기본 메시지로 대체
            errors.merge(
                    field, message, (a, b) -> a + ", " + b
            ); // 같은 필드에 여러 에러가 있을 수 있음. ex) "nickname": "닉네임은 필수입니다., 닉네임은 1자 이상이어야 합니다."
        });

        ApiResponse<Object> body = ApiResponse.onFailure(errors, ErrorCode.VALIDATION_ERROR);

        return handleExceptionInternal(
                e,
                body,
                headers,
                ErrorCode.VALIDATION_ERROR.getHttpStatus(),
                request
        );
    }

    // @RequestBody JSON 파싱 실패 또는 필드 타입/형식 불일치 시
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        ApiResponse<Object> body = ApiResponse.onFailure(null, ErrorCode.INVALID_REQUEST);

        return handleExceptionInternal(
                e,
                body,
                headers,
                ErrorCode.INVALID_REQUEST.getHttpStatus(),
                request
        );
    }

    // 도메인 CustomException 발생 시
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException e, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(
                null,
                e.getErrorCode(),
                e.getMessage()
        );

        return handleExceptionInternal(
                e,
                body,
                new HttpHeaders(),
                e.getErrorCode().getHttpStatus(),
                request
        );
    }

    // 처리되지 않은 모든 예외 -> 500 응답
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Exception e, WebRequest request) {
        log.error("Unhandled exception", e); // printStackTrace() 지양
        ApiResponse<Object> body = ApiResponse.onFailure(null, ErrorCode.INTERNAL_SERVER_ERROR); // 내부 메시지 노출 X

        return handleExceptionInternal(
                e,
                body,
                new HttpHeaders(),
                ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(),
                request
        );
    }
}
