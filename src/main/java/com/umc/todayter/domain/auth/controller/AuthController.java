package com.umc.todayter.domain.auth.controller;

import com.umc.todayter.domain.auth.dto.KakaoLoginResult;
import com.umc.todayter.domain.auth.dto.request.KakaoLoginRequest;
import com.umc.todayter.domain.auth.dto.response.KakaoLoginResponse;
import com.umc.todayter.domain.auth.dto.response.TokenResponse;
import com.umc.todayter.domain.auth.enums.code.AuthSuccessCode;
import com.umc.todayter.domain.auth.service.AuthTokenResult;
import com.umc.todayter.domain.auth.service.KakaoAuthService;
import com.umc.todayter.domain.auth.service.LogoutService;
import com.umc.todayter.domain.auth.service.TokenReissueService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.security.AuthOriginValidator;
import com.umc.todayter.global.security.SecurityUtil;
import com.umc.todayter.global.util.AuthCookieUtil;
import com.umc.todayter.global.util.GuestCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원 인증 및 소셜 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final GuestCookieUtil guestCookieUtil;
    private final AuthCookieUtil authCookieUtil;
    private final AuthOriginValidator authOriginValidator;
    private final KakaoAuthService kakaoAuthService;
    private final TokenReissueService tokenReissueService;
    private final LogoutService logoutService;

    @Operation(
            summary = "카카오 소셜 로그인",
            description = """
                    카카오 인가 코드로 사용자를 인증합니다.
                    기존 카카오 계정이면 로그인하고, 처음 로그인한 계정이면 회원과 소셜 계정을 생성합니다.
                    비회원 온보딩 정보가 존재하면 회원에게 이전한 뒤 JWT를 발급합니다.
                    """
    )
    @SecurityRequirements
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<KakaoLoginResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request,
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId,
            HttpServletResponse response
    ) {
        KakaoLoginResult result = kakaoAuthService.login(request.authorizationCode(), guestId);

        AuthTokenResult tokenResult = result.tokenResult();

        authCookieUtil.addRefreshTokenCookie(
                response,
                tokenResult.refreshToken(),
                tokenResult.refreshMaxAgeSeconds()
        );

        // 로그인 및 비회원 데이터 이전 완료 후 guest_id 만료
        guestCookieUtil.clearGuestCookie(response);

        KakaoLoginResponse responseBody = new KakaoLoginResponse(
                result.memberId(),
                tokenResult.response().accessToken(),
                result.newMember(),
                result.onboardingStep()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseBody, AuthSuccessCode.KAKAO_LOGIN_SUCCESS));
    }

    @Operation(
            summary = "토큰 재발급",
            description = """
                    HttpOnly 쿠키의 Refresh Token을 검증한 뒤
                    새로운 Access Token과 Refresh Token을 발급합니다.
                    기존 Refresh Token은 재사용할 수 없습니다.
                    """
    )
    @SecurityRequirements
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(
                    name = AuthCookieUtil.REFRESH_COOKIE_NAME,
                    required = false
            ) String refreshToken,
            HttpServletRequest request, // 추가
            HttpServletResponse response
    ) {
        authOriginValidator.validate(request); // 추가

        AuthTokenResult result = tokenReissueService.reissue(refreshToken);

        authCookieUtil.addRefreshTokenCookie(
                response,
                result.refreshToken(),
                result.refreshMaxAgeSeconds()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result.response(), AuthSuccessCode.TOKEN_REISSUED));
    }

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인한 회원의 Refresh Token 정보를 제거하고
                    브라우저의 Refresh Token 쿠키를 삭제합니다.
                    """
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        logoutService.logout(memberId);
        authCookieUtil.clearRefreshTokenCookie(response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(null, AuthSuccessCode.LOGOUT_SUCCESS));
    }
}
