package com.umc.todayter.domain.member.repository;

import com.umc.todayter.domain.member.entity.SocialAccount;
import com.umc.todayter.domain.member.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
    boolean existsByMemberIdAndProvider(Long memberId, SocialProvider provider);
}
