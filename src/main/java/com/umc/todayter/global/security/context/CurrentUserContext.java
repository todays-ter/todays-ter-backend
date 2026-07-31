package com.umc.todayter.global.security.context;

public record CurrentUserContext(
        CurrentUserType userType,
        Long memberId,
        Long guestSessionId,
        String guestId
) {

    public CurrentUserContext {
        validate(userType, memberId, guestSessionId, guestId);
    }

    public static CurrentUserContext forMember(Long memberId) {
        return new CurrentUserContext(CurrentUserType.MEMBER, memberId, null, null);
    }

    public static CurrentUserContext forGuest(Long guestSessionId, String guestId) {
        return new CurrentUserContext(CurrentUserType.GUEST, null, guestSessionId, guestId);
    }

    public boolean isMember() {
        return userType == CurrentUserType.MEMBER;
    }

    public boolean isGuest() {
        return userType == CurrentUserType.GUEST;
    }

    private static void validate(
            CurrentUserType userType,
            Long memberId,
            Long guestSessionId,
            String guestId
    ) {
        if (userType == null) {
            throw new IllegalArgumentException("userType must not be null");
        }

        if (userType == CurrentUserType.MEMBER) {
            validateMember(memberId, guestSessionId, guestId);
            return;
        }

        validateGuest(memberId, guestSessionId, guestId);
    }

    private static void validateMember(Long memberId, Long guestSessionId, String guestId) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId is required for MEMBER");
        }

        if (guestSessionId != null || guestId != null) {
            throw new IllegalArgumentException("guest fields must be null for MEMBER");
        }
    }

    private static void validateGuest(Long memberId, Long guestSessionId, String guestId) {
        if (memberId != null) {
            throw new IllegalArgumentException("memberId must be null for GUEST");
        }

        if (guestSessionId == null) {
            throw new IllegalArgumentException("guestSessionId is required for GUEST");
        }

        if (guestId == null || guestId.isBlank()) {
            throw new IllegalArgumentException("guestId is required for GUEST");
        }
    }
}
