package com.umc.todayter.global.util;

import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.config.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String upload(MultipartFile file, String keyPrefix) {
        String extension = extractExtension(file);
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
        } catch (IOException | S3Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
        }

        return "https://%s.s3.%s.amazonaws.com/%s".formatted(s3Properties.bucket(), s3Properties.region(), key);
    }

    private String extractExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "빈 파일은 업로드할 수 없습니다.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "파일 형식을 확인할 수 없습니다.");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "허용되지 않는 이미지 형식입니다. (jpg, jpeg, png, webp만 허용)");
        }

        return extension;
    }
}
