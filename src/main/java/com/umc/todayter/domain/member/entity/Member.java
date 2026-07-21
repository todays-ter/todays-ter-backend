package com.umc.todayter.domain.member.entity;

import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member extends BaseEntity {

    @Column(length = 50)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(name = "refresh_token_hash", length = 64)
    private String refreshTokenHash;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    @Column(name = "withdraw_reason")
    private String withdrawReason;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    private Member(String nickname) {
        this.nickname = nickname;
        this.status = MemberStatus.ACTIVE;
    }

    private Member(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member create(String nickname) {
        return new Member(nickname);
    }

    public static Member create(String email, String nickname) {
        return new Member(email, nickname);
    }

    public void updateRefreshToken(String refreshTokenHash, LocalDateTime expiresAt) {
        this.refreshTokenHash = refreshTokenHash;
        this.refreshTokenExpiresAt = expiresAt;
    }

    public void clearRefreshToken() {
        this.refreshTokenHash = null;
        this.refreshTokenExpiresAt = null;
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }
}
