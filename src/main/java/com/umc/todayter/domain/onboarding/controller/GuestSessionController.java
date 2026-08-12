package com.umc.todayter.domain.onboarding.controller;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.service.MemberService;
import com.umc.todayter.domain.onboarding.dto.response.GuestSessionResponse;
import com.umc.todayter.domain.onboarding.dto.response.GuestSessionStatusResponse;
import com.umc.todayter.domain.onboarding.enums.code.GuestSuccessCode;
import com.umc.todayter.domain.onboarding.exception.code.OnboardingErrorCode;
import com.umc.todayter.domain.onboarding.service.GuestOnboardingTransferService;
import com.umc.todayter.domain.onboarding.service.GuestSessionService;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.security.SecurityUtil;
import com.umc.todayter.global.util.GuestCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Guest Session", description = "비회원 세션 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest-sessions")
public class GuestSessionController {

    private final GuestSessionService guestSessionService;
    private final MemberService memberService;
    private final GuestOnboardingTransferService guestOnboardingTransferService;
    private final GuestCookieUtil guestCookieUtil;

    @Operation(
            summary = "비회원 세션 생성·조회",
            description = "비회원 세션을 생성하거나 기존 세션을 조회합니다."
    )
    @SecurityRequirements
    @PostMapping
    public ResponseEntity<ApiResponse<GuestSessionResponse>> initialize(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId,
            HttpServletResponse response
    ) {
        GuestSessionService.GuestSessionResult result = guestSessionService.initialize(guestId);

        if (result.cookieRefreshRequired()) {
            guestCookieUtil.addGuestCookie(response, result.guestId());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result.response(), SuccessCode.OK));
    }

    @Operation(
            summary = "비회원 쿠키 존재 여부 조회",
            description = "현재 요청에 guest_id 쿠키가 존재하는지 확인합니다."
    )
    @SecurityRequirements
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<GuestSessionStatusResponse>> getGuestSessionStatus(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId
    ) {
        boolean hasGuestId = StringUtils.hasText(guestId);

        GuestSessionStatusResponse result = new GuestSessionStatusResponse(hasGuestId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, GuestSuccessCode.GUEST_SESSION_STATUS_RETRIEVED));
    }

    @Operation(
            summary = "비회원 온보딩 회원 전환",
            description = "현재 guest_id에 연결된 비회원 온보딩 정보를 로그인한 회원 계정으로 이전합니다."
    )
    @PostMapping("/convert")
    public ResponseEntity<ApiResponse<Void>> convertGuestSession(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId,
            HttpServletResponse response
    ) {
        if (!StringUtils.hasText(guestId)) {
            throw new CustomException(OnboardingErrorCode.GUEST_COOKIE_REQUIRED);
        }

        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberService.getActiveMember(memberId);

        guestOnboardingTransferService.transferIfPresent(guestId, member);

        guestCookieUtil.clearGuestCookie(response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(null, GuestSuccessCode.GUEST_SESSION_CONVERTED));
    }
}
