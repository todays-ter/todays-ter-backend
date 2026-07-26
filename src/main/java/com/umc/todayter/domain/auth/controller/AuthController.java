package com.umc.todayter.domain.auth.controller;

import com.umc.todayter.domain.auth.dto.KakaoLoginResult;
import com.umc.todayter.domain.auth.dto.request.KakaoLoginRequest;
import com.umc.todayter.domain.auth.dto.response.KakaoLoginResponse;
import com.umc.todayter.domain.auth.enums.code.AuthSuccessCode;
import com.umc.todayter.domain.auth.service.AuthTokenResult;
import com.umc.todayter.domain.auth.service.KakaoAuthService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.util.AuthCookieUtil;
import com.umc.todayter.global.util.GuestCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final KakaoAuthService kakaoAuthService;
    private final AuthCookieUtil authCookieUtil;

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
}
