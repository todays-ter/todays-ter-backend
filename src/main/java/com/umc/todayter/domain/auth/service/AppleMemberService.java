package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.dto.AppleUserInfo;
import com.umc.todayter.domain.auth.dto.SocialMemberResult;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.entity.SocialAccount;
import com.umc.todayter.domain.member.enums.SocialProvider;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.member.repository.SocialAccountRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleMemberService {

    private static final String DEFAULT_NICKNAME = "오늘의 터 사용자";

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    /**
     * 애플 사용자 정보를 기준으로 기존 사용자를 조회하거나 신규 사용자를 생성
     * 회원 식별 기준은 이메일이 아닌, 소셜 로그인 제공자와 제공자 사용자 ID의 조합
     */
    @Transactional
    public SocialMemberResult findOrCreate(AppleUserInfo userInfo) {
        return socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.APPLE, userInfo.providerUserId())

                // 기존 소셜 계정이 존재하면 사용자 상태 확인 및 정보 갱신
                .map(socialAccount -> handleExistingAccount(socialAccount, userInfo))

                // 기존 계정이 없으면 사용자와 소셜 계정을 새로 생성
                .orElseGet(() -> createMemberAndSocialAccount(userInfo));
    }

    // 기존 애플 계정으로 로그인한 사용자를 처리함
    private SocialMemberResult handleExistingAccount(SocialAccount socialAccount, AppleUserInfo userInfo) {
        Member member = socialAccount.getMember();

        if (!member.isActive()) {
            throw new CustomException(MemberErrorCode.MEMBER_INACTIVE);
        }

        if (StringUtils.hasText(userInfo.email())) {
            socialAccount.updateProviderEmail(userInfo.email());
        }

        return SocialMemberResult.existing(member);
    }

    // 신규 사용자와 해당 사용자의 애플 소셜 계정을 생성함
    private SocialMemberResult createMemberAndSocialAccount(AppleUserInfo userInfo) {
        Member member = Member.create(userInfo.email(), DEFAULT_NICKNAME);

        memberRepository.save(member);

        /**
         * 애플 Identity Token의 sub를 기준으로 소셜 계정 생성
         * providerUserId는 동일한 애플 사용자를 식별하는 핵심 값
         */
        SocialAccount socialAccount = SocialAccount.create(
                member,
                SocialProvider.APPLE,
                userInfo.providerUserId(),
                userInfo.email()
        );

        socialAccountRepository.save(socialAccount);

        return SocialMemberResult.created(member);
    }
}
