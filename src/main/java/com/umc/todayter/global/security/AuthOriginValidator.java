package com.umc.todayter.global.security;

import com.umc.todayter.domain.auth.exception.AuthErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Component
public class AuthOriginValidator {

    private static final Set<String> LOCAL_ORIGINS = Set.of(
            "http://localhost:3000",
            "http://localhost:5173"
    );

    private static final String PRODUCTION_ORIGIN = "https://todays-ter-frontend.vercel.app";

    private static final String VERCEL_PREVIEW_HOST_PREFIX = "todays-ter-frontend-";

    private static final String VERCEL_HOST_SUFFIX = ".vercel.app";

    public void validate(HttpServletRequest request) {
        String origin = request.getHeader("Origin");

        if (!isAllowedOrigin(origin)) {
            throw new CustomException(AuthErrorCode.INVALID_ORIGIN);
        }
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }

        if (LOCAL_ORIGINS.contains(origin) || PRODUCTION_ORIGIN.equals(origin)) {
            return true;
        }

        try {
            URI uri = new URI(origin);
            String host = uri.getHost();

            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getPort() == -1
                    && uri.getUserInfo() == null
                    && host != null
                    && host.startsWith(VERCEL_PREVIEW_HOST_PREFIX)
                    && host.endsWith(VERCEL_HOST_SUFFIX);

        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
