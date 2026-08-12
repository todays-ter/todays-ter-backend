package com.umc.todayter.global.util;

import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.config.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

/**
 * S3 업로드만 담당하는 순수 인프라 유틸리티.
 * 파일 개수/확장자/용량 등 업로드 정책 검증은 도메인 서비스(RecordService)에서 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(15);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public String upload(MultipartFile file, String keyPrefix) {
        String extension = extractExtension(file.getOriginalFilename());
        String key = "%s/%s.%s".formatted(keyPrefix, UUID.randomUUID(), extension);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.bucket())
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException | SdkException e) {
            // SdkException은 서비스 응답 오류(S3Exception 등)와 클라이언트 측 오류(SdkClientException,
            // 네트워크/타임아웃 등)의 공통 상위 타입이라 둘 다 여기서 함께 처리된다.
            log.error("S3 업로드 실패: bucket={}, key={}", s3Properties.bucket(), key, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
        }

        return "https://%s.s3.%s.amazonaws.com/%s".formatted(s3Properties.bucket(), s3Properties.region(), key);
    }

    // 업로드 후 DB 저장이 실패했을 때 이미 올라간 S3 객체를 정리하기 위한 best-effort 삭제.
    // 정리 자체가 실패해도 원래 예외를 가리지 않도록 여기서 삼킨다.
    public void delete(String imageUrl) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(extractKey(imageUrl))
                    .build());
        } catch (Exception e) {
            log.warn("S3 객체 정리 실패, 수동 확인 필요: {}", imageUrl, e);
        }
    }

    // 버킷을 퍼블릭으로 열지 않고도 조회할 수 있도록, 저장된(퍼블릭 형태) URL 대신
    // 요청 시점마다 짧게 유효한 서명된 URL을 새로 발급해서 내려준다.
    public String presignedGetUrl(String imageUrl) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(extractKey(imageUrl))
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_DURATION)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("이미지 조회 URL 서명 실패: {}", imageUrl, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 조회에 실패했습니다.");
        }
    }

    private String extractKey(String imageUrl) {
        return URI.create(imageUrl).getPath().replaceFirst("^/", "");
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
