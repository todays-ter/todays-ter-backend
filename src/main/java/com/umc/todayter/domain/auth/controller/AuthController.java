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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final KakaoAuthService kakaoAuthService;
    private final AuthCookieUtil authCookieUtil;

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
