package com.umc.todayter.domain.record.controller;

import com.umc.todayter.domain.record.dto.request.RecordCreateRequest;
import com.umc.todayter.domain.record.dto.response.ImageInfo;
import com.umc.todayter.domain.record.dto.response.ImageUploadResponse;
import com.umc.todayter.domain.record.dto.response.RecordResponse;
import com.umc.todayter.domain.record.service.RecordService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Record", description = "다녀온 터 기록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/records")
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "다녀온 터 기록/후기 이미지 업로드", description = "기록·후기에 첨부할 이미지를 S3에 업로드하고, 이후 작성 API에서 참조할 imageId 목록을 반환합니다. 최대 5장, 파일당 최대 10MB, jpg/jpeg/png만 허용합니다.")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImages(
            @RequestParam("images") List<MultipartFile> images
    ) {
        List<ImageInfo> uploaded = recordService.uploadImages(images);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(new ImageUploadResponse(uploaded), SuccessCode.OK));
    }

    @Operation(summary = "다녀온 터 기록/후기 작성", description = "장소, 유형(RECORD/REVIEW), 평점, 내용, 사전 업로드된 imageId 목록으로 기록 또는 후기를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @RequestBody @Valid RecordCreateRequest request
    ) {
        RecordResponse result = recordService.createRecord(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess(result, SuccessCode.CREATED));
    }
}
