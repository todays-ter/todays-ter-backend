package com.umc.todayter.domain.onboarding.controller;

import com.umc.todayter.domain.onboarding.dto.request.GuestConcernRequest;
import com.umc.todayter.domain.onboarding.dto.request.GuestSajuRequest;
import com.umc.todayter.domain.onboarding.dto.response.GuestConcernResponse;
import com.umc.todayter.domain.onboarding.dto.response.GuestSajuResponse;
import com.umc.todayter.domain.onboarding.enums.code.OnboardingSuccessCode;
import com.umc.todayter.domain.onboarding.service.GuestOnboardingService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.util.GuestCookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest-onboarding")
public class GuestOnboardingController {

    private final GuestOnboardingService guestOnboardingService;

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
}
