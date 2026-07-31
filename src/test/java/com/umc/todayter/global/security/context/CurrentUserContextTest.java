package com.umc.todayter.global.security.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserContextTest {

    @Test
    void forMember_createsMemberContext() {
        CurrentUserContext context = CurrentUserContext.forMember(1L);

        assertThat(context.userType()).isEqualTo(CurrentUserType.MEMBER);
        assertThat(context.memberId()).isEqualTo(1L);
        assertThat(context.guestSessionId()).isNull();
        assertThat(context.guestId()).isNull();
        assertThat(context.isMember()).isTrue();
        assertThat(context.isGuest()).isFalse();
    }

    @Test
    void forGuest_createsGuestContext() {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");

        assertThat(context.userType()).isEqualTo(CurrentUserType.GUEST);
        assertThat(context.memberId()).isNull();
        assertThat(context.guestSessionId()).isEqualTo(10L);
        assertThat(context.guestId()).isEqualTo("guest-id");
        assertThat(context.isMember()).isFalse();
        assertThat(context.isGuest()).isTrue();
    }

    @Test
    void nullUserTypeCannotBeCreated() {
        assertThatThrownBy(() -> new CurrentUserContext(null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void memberWithoutMemberId_fails() {
        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.MEMBER, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void memberWithGuestFields_fails() {
        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.MEMBER, 1L, 10L, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.MEMBER, 1L, null, "guest-id"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void guestWithMemberId_fails() {
        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.GUEST, 1L, 10L, "guest-id"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void guestWithoutGuestSessionId_fails() {
        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.GUEST, null, null, "guest-id"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void guestWithoutGuestId_fails() {
        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.GUEST, null, 10L, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.GUEST, null, 10L, ""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new CurrentUserContext(CurrentUserType.GUEST, null, 10L, "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
