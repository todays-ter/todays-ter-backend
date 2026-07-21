package com.umc.todayter.domain.auth.dto.response;

// Refresh Token은 JSON으로 반환하지 않고 HttpOnly 쿠키로만 전달
public record TokenResponse(String accessToken) {
}
