package com.umc.todayter.domain.onboarding.entity;

import com.umc.todayter.domain.onboarding.enums.GuestSessionStatus;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "guest_sessions",
        uniqueConstraints = {
                @UniqueConstraint( // 제약조건
                        name = "uk_guest_sessions_guest_id", // uk_테이블명_컬럼명 (uk = Unique Key)
                        columnNames = "guest_id"
                )
        }
)
public class GuestSession extends BaseEntity {

    @Column(name = "guest_id", nullable = false)
    private String guestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuestSessionStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "converted_member_id")
    private Long convertedMemberId;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    private GuestSession(String guestId, LocalDateTime expiresAt) {
        this.guestId = guestId;
        this.status = GuestSessionStatus.ACTIVE;
        this.expiresAt = expiresAt;
    }

    // 새 비회원 세션 생성
    public static GuestSession create(String guestId, LocalDateTime expiresAt) {
        return new GuestSession(guestId, expiresAt);
    }

    // 현재 비회원 세션이 사용 가능한지 검사
    public boolean isUsable(LocalDateTime now) {
        return status == GuestSessionStatus.ACTIVE && expiresAt.isAfter(now);
    }

    // 세션 만료 처리
    public void expire() {
        this.status = GuestSessionStatus.EXPIRED;
    }
}
