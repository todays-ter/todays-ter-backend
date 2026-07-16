package com.umc.todayter.domain.onboarding.controller;

import com.umc.todayter.domain.onboarding.dto.response.GuestSessionResponse;
import com.umc.todayter.domain.onboarding.service.GuestSessionService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.util.GuestCookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest-sessions")
public class GuestSessionController {

    private final GuestSessionService guestSessionService;
    private final GuestCookieUtil guestCookieUtil;

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
}
