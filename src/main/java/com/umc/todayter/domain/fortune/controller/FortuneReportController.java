package com.umc.todayter.domain.fortune.controller;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportCreateResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportStatusResponse;
import com.umc.todayter.domain.fortune.enums.code.FortuneReportSuccessCode;
import com.umc.todayter.domain.fortune.service.FortuneReportService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Fortune Report", description = "사주 리포트 생성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune-reports")
public class FortuneReportController {

    private final FortuneReportService fortuneReportService;

    @Operation(summary = "리포트 생성", description = "온보딩 정보를 사용해 비동기 리포트 생성을 시작합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<FortuneReportCreateResponse>> create() {
        FortuneReportCreateResponse result = fortuneReportService.create(SecurityUtil.getCurrentMemberId());
        return ResponseEntity
                .status(FortuneReportSuccessCode.GENERATION_ACCEPTED.getHttpStatus())
                .body(ApiResponse.onSuccess(result, FortuneReportSuccessCode.GENERATION_ACCEPTED));
    }

    @Operation(summary = "리포트 생성 상태 조회", description = "리포트 상태와 0~100 진행률을 조회합니다.")
    @GetMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<FortuneReportStatusResponse>> getStatus(@PathVariable Long reportId) {
        FortuneReportStatusResponse result = fortuneReportService.getStatus(
                SecurityUtil.getCurrentMemberId(), reportId
        );
        return ResponseEntity.ok(ApiResponse.onSuccess(result, FortuneReportSuccessCode.STATUS_FOUND));
    }

    @Operation(summary = "실패한 리포트 재시도", description = "실패한 리포트를 같은 ID로 다시 생성합니다.")
    @PostMapping("/{reportId}/retry")
    public ResponseEntity<ApiResponse<FortuneReportStatusResponse>> retry(@PathVariable Long reportId) {
        FortuneReportStatusResponse result = fortuneReportService.retry(
                SecurityUtil.getCurrentMemberId(), reportId
        );
        return ResponseEntity
                .status(FortuneReportSuccessCode.RETRY_ACCEPTED.getHttpStatus())
                .body(ApiResponse.onSuccess(result, FortuneReportSuccessCode.RETRY_ACCEPTED));
    }
}
