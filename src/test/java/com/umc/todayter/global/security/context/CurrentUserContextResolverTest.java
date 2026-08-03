package com.umc.todayter.global.security.context;

import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.enums.GuestSessionStatus;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.security.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserContextResolverTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-07-31T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private GuestSessionRepository guestSessionRepository;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validAuthPrincipalReturnsMemberContext() {
        setMemberAuthentication(1L);
        CurrentUserContextResolver resolver = resolver();

        CurrentUserContext context = resolver.resolve(null);

        assertThat(context.userType()).isEqualTo(CurrentUserType.MEMBER);
        assertThat(context.memberId()).isEqualTo(1L);
        assertThat(context.guestSessionId()).isNull();
        assertThat(context.guestId()).isNull();
    }

    @Test
    void validGuestIdReturnsGuestContext() {
        GuestSession guestSession = guestSession(10L, "guest-id", LocalDateTime.of(2026, 7, 31, 10, 1));
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.of(guestSession));

        CurrentUserContext context = resolver().resolve("guest-id");

        assertThat(context.userType()).isEqualTo(CurrentUserType.GUEST);
        assertThat(context.memberId()).isNull();
        assertThat(context.guestSessionId()).isEqualTo(10L);
        assertThat(context.guestId()).isEqualTo("guest-id");
    }

    @Test
    void memberAuthenticationWinsOverGuestId() {
        setMemberAuthentication(1L);

        CurrentUserContext context = resolver().resolve("guest-id");

        assertThat(context.userType()).isEqualTo(CurrentUserType.MEMBER);
        assertThat(context.memberId()).isEqualTo(1L);
        verify(guestSessionRepository, never()).findByGuestId(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void memberAuthenticationWinsOverInvalidGuestId() {
        setMemberAuthentication(1L);

        CurrentUserContext context = resolver().resolve("wrong-guest-id");

        assertThat(context.userType()).isEqualTo(CurrentUserType.MEMBER);
        assertThat(context.memberId()).isEqualTo(1L);
        verify(guestSessionRepository, never()).findByGuestId(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void noAuthenticationWithActiveGuestSessionReturnsGuest() {
        GuestSession guestSession = guestSession(10L, "guest-id", LocalDateTime.of(2026, 7, 31, 10, 1));
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.of(guestSession));

        CurrentUserContext context = resolver().resolve("guest-id");

        assertThat(context.userType()).isEqualTo(CurrentUserType.GUEST);
        assertThat(context.guestSessionId()).isEqualTo(10L);
        assertThat(context.guestId()).isEqualTo("guest-id");
    }

    @Test
    void missingAuthenticationAndCookieThrowsUnauthorized() {
        assertUnauthorized(() -> resolver().resolve(null));
    }

    @Test
    void blankGuestIdThrowsUnauthorized() {
        assertUnauthorized(() -> resolver().resolve(""));
        assertUnauthorized(() -> resolver().resolve("   "));
    }

    @Test
    void missingGuestSessionThrowsUnauthorized() {
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.empty());

        assertUnauthorized(() -> resolver().resolve("guest-id"));
    }

    @Test
    void activeGuestSessionExpiringNowThrowsUnauthorized() {
        GuestSession guestSession = guestSession(10L, "guest-id", LocalDateTime.of(2026, 7, 31, 10, 0));
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.of(guestSession));

        assertUnauthorized(() -> resolver().resolve("guest-id"));
    }

    @Test
    void activeGuestSessionExpiredInPastThrowsUnauthorized() {
        GuestSession guestSession = guestSession(10L, "guest-id", LocalDateTime.of(2026, 7, 31, 9, 59));
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.of(guestSession));

        assertUnauthorized(() -> resolver().resolve("guest-id"));
    }

    @Test
    void expiredGuestSessionThrowsUnauthorized() {
        GuestSession guestSession = guestSession(10L, "guest-id", LocalDateTime.of(2026, 7, 31, 10, 1));
        guestSession.expire();
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.of(guestSession));

        assertUnauthorized(() -> resolver().resolve("guest-id"));
    }

    @Test
    void convertedGuestSessionThrowsUnauthorized() {
        GuestSession guestSession = guestSession(10L, "guest-id", LocalDateTime.of(2026, 7, 31, 10, 1));
        ReflectionTestUtils.setField(guestSession, "status", GuestSessionStatus.CONVERTED);
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.of(guestSession));

        assertUnauthorized(() -> resolver().resolve("guest-id"));
    }

    private CurrentUserContextResolver resolver() {
        return new CurrentUserContextResolver(guestSessionRepository, FIXED_CLOCK);
    }

    private void setMemberAuthentication(Long memberId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthPrincipal(memberId), null, Collections.emptyList())
        );
    }

    private GuestSession guestSession(Long id, String guestId, LocalDateTime expiresAt) {
        GuestSession guestSession = GuestSession.create(guestId, expiresAt);
        ReflectionTestUtils.setField(guestSession, "id", id);
        return guestSession;
    }

    private void assertUnauthorized(ThrowingCallable callable) {
        assertThatThrownBy(callable::call)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @FunctionalInterface
    private interface ThrowingCallable {
        void call();
    }
}
