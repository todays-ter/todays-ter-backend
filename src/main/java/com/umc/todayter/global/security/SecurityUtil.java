package com.umc.todayter.global.security;

import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getCurrentMemberId() {
        Long memberId = getCurrentMemberIdOrNull();
        if (memberId != null) {
            return memberId;
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    public static Long getCurrentMemberIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthPrincipal authPrincipal) {
            return authPrincipal.getMemberId();
        }
        return null;
    }
}
