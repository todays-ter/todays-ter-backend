package com.umc.todayter.domain.fortune.controller;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportCreateResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportStatusResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportDetailResponse;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportSummaryResponse;
import com.umc.todayter.domain.fortune.service.FortuneReportService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.dto.response.ShareLinkResponse;
import com.umc.todayter.global.security.SecurityUtil;
import com.umc.todayter.global.util.GuestCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
    @SecurityRequirements
    @PostMapping
    public ResponseEntity<ApiResponse<FortuneReportCreateResponse>> create(
            @CookieValue(name = GuestCookieUtil.COOKIE_NAME, required = false) String guestId
    ) {
        FortuneReportCreateResponse result = fortuneReportService.create(
                SecurityUtil.getCurrentMemberIdOrNull(), guestId
        );
        return ResponseEntity
                .status(SuccessCode.ACCEPTED.getHttpStatus())
                .body(ApiResponse.onSuccess(result, SuccessCode.ACCEPTED));
    }

    @Operation(summary = "리포트 생성 상태 조회", description = "리포트 상태와 0~100 진행률을 조회합니다.")
    @SecurityRequirements
    @GetMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<FortuneReportStatusResponse>> getStatus(
            @CookieValue(name = GuestCookieUtil.COOKIE_NAME, required = false) String guestId,
            @PathVariable Long reportId
    ) {
        FortuneReportStatusResponse result = fortuneReportService.getStatus(
                SecurityUtil.getCurrentMemberIdOrNull(), guestId, reportId
        );
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "기본 리포트 조회", description = "완료된 리포트의 기본 요약과 오행 분포를 조회합니다.")
    @SecurityRequirements
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<FortuneReportSummaryResponse>> getSummary(
            @CookieValue(name = GuestCookieUtil.COOKIE_NAME, required = false) String guestId,
            @PathVariable Long reportId
    ) {
        FortuneReportSummaryResponse result = fortuneReportService.getSummary(
                SecurityUtil.getCurrentMemberIdOrNull(), guestId, reportId
        );
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "상세 리포트 조회", description = "카테고리별 상세 분석을 조회합니다.")
    @SecurityRequirements
    @GetMapping("/{reportId}/details")
    public ResponseEntity<ApiResponse<FortuneReportDetailResponse>> getDetail(
            @CookieValue(name = GuestCookieUtil.COOKIE_NAME, required = false) String guestId,
            @PathVariable Long reportId,
            @RequestParam String category
    ) {
        FortuneReportDetailResponse result = fortuneReportService.getDetail(
                SecurityUtil.getCurrentMemberIdOrNull(), guestId, reportId, category
        );
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "리포트 공유 링크 생성", description = "완료된 본인 리포트를 비로그인 사용자도 볼 수 있는 공유 링크를 생성합니다.")
    @SecurityRequirements
    @PostMapping("/{reportId}/share")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @CookieValue(name = GuestCookieUtil.COOKIE_NAME, required = false) String guestId,
            @PathVariable Long reportId
    ) {
        ShareLinkResponse result = fortuneReportService.createShareLink(
                SecurityUtil.getCurrentMemberIdOrNull(), guestId, reportId
        );
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "공유 리포트 카테고리 조회", description = "공유 토큰으로 연애, 건강 등 모든 상세 카테고리를 공개 조회합니다.")
    @SecurityRequirements
    @GetMapping("/shared/{shareToken}/details")
    public ResponseEntity<ApiResponse<FortuneReportDetailResponse>> getSharedDetail(
            @PathVariable String shareToken,
            @RequestParam String category
    ) {
        FortuneReportDetailResponse result = fortuneReportService.getSharedDetail(shareToken, category);
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "실패한 리포트 재시도", description = "실패한 리포트를 같은 ID로 다시 생성합니다.")
    @SecurityRequirements
    @PostMapping("/{reportId}/retry")
    public ResponseEntity<ApiResponse<FortuneReportStatusResponse>> retry(
            @CookieValue(name = GuestCookieUtil.COOKIE_NAME, required = false) String guestId,
            @PathVariable Long reportId
    ) {
        FortuneReportStatusResponse result = fortuneReportService.retry(
                SecurityUtil.getCurrentMemberIdOrNull(), guestId, reportId
        );
        return ResponseEntity
                .status(SuccessCode.ACCEPTED.getHttpStatus())
                .body(ApiResponse.onSuccess(result, SuccessCode.ACCEPTED));
    }
}
