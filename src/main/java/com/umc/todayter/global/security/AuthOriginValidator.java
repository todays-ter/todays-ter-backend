package com.umc.todayter.global.security;

import com.umc.todayter.domain.auth.exception.AuthErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthOriginValidator {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://todays-ter-frontend.vercel.app"
    );

    public void validate(HttpServletRequest request) {
        String origin = request.getHeader("Origin");

        if (origin == null || !ALLOWED_ORIGINS.contains(origin)) {
            throw new CustomException(AuthErrorCode.INVALID_ORIGIN);
        }
    }
}
