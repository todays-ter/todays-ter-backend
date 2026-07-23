package com.umc.todayter.domain.auth.controller;

import com.umc.todayter.domain.auth.dto.request.DevTokenRequest;
import com.umc.todayter.domain.auth.dto.response.DevTokenResponse;
import com.umc.todayter.domain.auth.enums.code.AuthSuccessCode;
import com.umc.todayter.domain.auth.service.AuthTokenResult;
import com.umc.todayter.domain.auth.service.DevAuthService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.util.AuthCookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/dev")
public class DevAuthController {

    private final DevAuthService devAuthService;
    private final AuthCookieUtil authCookieUtil;

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
