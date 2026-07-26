package com.umc.todayter.domain.member.entity;

import com.umc.todayter.domain.member.enums.SocialProvider;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                // 같은 소셜 계정이 여러 사용자에게 중복 연결되는 것을 막음
                @UniqueConstraint(
                        name = "uk_social_accounts_provider_user",
                        columnNames = {
                                "provider",
                                "provider_user_id"
                        }
                ),
                // ex) 한 사용자에게 카카오 계정 두 개가 연결되는 것을 막음
                @UniqueConstraint(
                        name = "uk_social_accounts_member_provider",
                        columnNames = {
                                "member_id",
                                "provider"
                        }
                )
        }
)
public class SocialAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_social_accounts_member"
            )
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "provider_email", length = 100)
    private String providerEmail;

    private SocialAccount(
            Member member,
            SocialProvider provider,
            String providerUserId,
            String providerEmail
    ) {
        this.member = member;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
    }

    public static SocialAccount create(
            Member member,
            SocialProvider provider,
            String providerUserId,
            String providerEmail
    ) {
        return new SocialAccount(
                member,
                provider,
                providerUserId,
                providerEmail
        );
    }

    public void updateProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }
}
