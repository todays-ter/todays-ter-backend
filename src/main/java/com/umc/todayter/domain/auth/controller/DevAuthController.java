package com.umc.todayter.domain.auth.controller;

import com.umc.todayter.domain.auth.dto.request.DevTokenRequest;
import com.umc.todayter.domain.auth.dto.response.DevTokenResponse;
import com.umc.todayter.domain.auth.enums.code.AuthSuccessCode;
import com.umc.todayter.domain.auth.service.AuthTokenResult;
import com.umc.todayter.domain.auth.service.DevAuthService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.util.AuthCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dev Auth", description = "개발 및 테스트용 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/dev")
public class DevAuthController {

    private final DevAuthService devAuthService;
    private final AuthCookieUtil authCookieUtil;

    @Operation(
            summary = "개발용 토큰 발급",
            description = "소셜 로그인 없이 개발 및 API 테스트에 사용할 Access Token과 Refresh Token을 발급합니다."
    )
    @SecurityRequirements
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<DevTokenResponse>> issueToken(
            @Valid @RequestBody DevTokenRequest request,
            HttpServletResponse response
    ) {
        DevAuthService.DevTokenResult result = devAuthService.issueToken(request);

        AuthTokenResult tokenResult = result.tokenResult();

        authCookieUtil.addRefreshTokenCookie(
                response,
                tokenResult.refreshToken(),
                tokenResult.refreshMaxAgeSeconds()
        );

        DevTokenResponse responseBody = new DevTokenResponse(
                result.memberId(),
                tokenResult.response().accessToken()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseBody, AuthSuccessCode.DEV_TOKEN_ISSUED));
    }
}
