package com.umc.todayter.domain.onboarding.controller;

import com.umc.todayter.domain.onboarding.dto.request.GuestConcernRequest;
import com.umc.todayter.domain.onboarding.dto.request.GuestSajuRequest;
import com.umc.todayter.domain.onboarding.dto.response.GuestConcernResponse;
import com.umc.todayter.domain.onboarding.dto.response.GuestOnboardingResponse;
import com.umc.todayter.domain.onboarding.dto.response.GuestSajuResponse;
import com.umc.todayter.domain.onboarding.enums.code.OnboardingSuccessCode;
import com.umc.todayter.domain.onboarding.service.GuestOnboardingService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.util.GuestCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Guest Onboarding", description = "비회원 온보딩 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest-onboarding")
public class GuestOnboardingController {

    private final GuestOnboardingService guestOnboardingService;

    @Operation(
            summary = "사주 정보 저장·수정",
            description = "달력 유형, 생년월일, 출생 시간, 출생 시간 모름 여부를 저장하거나 수정합니다."
    )
    @SecurityRequirements
    @PutMapping("/saju")
    public ResponseEntity<ApiResponse<GuestSajuResponse>> saveSaju(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId,
            @Valid @RequestBody GuestSajuRequest request
    ) {
        GuestSajuResponse result = guestOnboardingService.saveSaju(guestId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, OnboardingSuccessCode.SAJU_SAVED));
    }

    @Operation(
            summary = "고민 유형 저장·수정",
            description = "선택한 고민 유형을 저장하거나 기존 선택 내용을 전체 교체합니다."
    )
    @SecurityRequirements
    @PutMapping("/concerns")
    public ResponseEntity<ApiResponse<GuestConcernResponse>> saveConcerns(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId,
            @Valid @RequestBody GuestConcernRequest request
    ) {
        GuestConcernResponse result = guestOnboardingService.saveConcerns(guestId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, OnboardingSuccessCode.CONCERNS_SAVED));
    }

    @Operation(
            summary = "비회원 온보딩 조회",
            description = "비회원 세션에 저장된 사주 정보, 고민 유형 및 현재 온보딩 진행 단계를 조회합니다."
    )
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<ApiResponse<GuestOnboardingResponse>> getOnboarding(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId
    ) {
        GuestOnboardingResponse result = guestOnboardingService.getOnboarding(guestId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }
}
