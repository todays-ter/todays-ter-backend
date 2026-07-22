package com.umc.todayter.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class GuestCookieUtil {

    public static final String COOKIE_NAME = "guest_id";
    public static final Duration COOKIE_MAX_AGE = Duration.ofDays(30);

    public void addGuestCookie(HttpServletResponse response, String guestId) {
        ResponseCookie cookie = ResponseCookie
                .from(COOKIE_NAME, guestId)
                .httpOnly(true)
                .secure(false) // 운영 환경에서는 true
                .sameSite("Lax")
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
