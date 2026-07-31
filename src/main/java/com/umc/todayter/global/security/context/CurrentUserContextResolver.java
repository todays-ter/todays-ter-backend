package com.umc.todayter.global.security.context;

import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CurrentUserContextResolver {

    private final GuestSessionRepository guestSessionRepository;
    private final Clock clock;

    public CurrentUserContext resolve(String guestId) {
        CurrentUserContext memberContext = resolveMemberContext();

        if (memberContext != null) {
            return memberContext;
        }

        return resolveGuestContext(guestId);
    }

    private CurrentUserContext resolveMemberContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthPrincipal authPrincipal) || authPrincipal.getMemberId() == null) {
            return null;
        }

        return CurrentUserContext.forMember(authPrincipal.getMemberId());
    }

    private CurrentUserContext resolveGuestContext(String guestId) {
        if (guestId == null || guestId.isBlank()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        GuestSession guestSession = guestSessionRepository
                .findByGuestId(guestId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        if (!guestSession.isUsable(LocalDateTime.now(clock))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return CurrentUserContext.forGuest(guestSession.getId(), guestSession.getGuestId());
    }
}
