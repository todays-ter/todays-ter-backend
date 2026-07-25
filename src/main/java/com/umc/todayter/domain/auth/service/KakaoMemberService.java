package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.dto.KakaoUserInfo;
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
public class KakaoMemberService {

    private static final String DEFAULT_NICKNAME = "오늘의 터 사용자";

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    /**
     * 카카오 사용자 정보를 기준으로 기존 사용자를 조회하거나 신규 사용자를 생성
     * 회원 식별 기준은 이메일이 아닌, 소셜 로그인 제공자와 제공자 사용자 ID의 조합
     */
    @Transactional
    public SocialMemberResult findOrCreate(KakaoUserInfo userInfo) {
        return socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.KAKAO, userInfo.providerUserId())

                // 기존 소셜 계정이 존재하면 사용자 상태 확인 및 정보 갱신
                .map(socialAccount -> handleExistingAccount(socialAccount, userInfo))

                // 기존 계정이 없으면 사용자와 소셜 계정을 새로 생성
                .orElseGet(() -> createMemberAndSocialAccount(userInfo));
    }

    // 기존 카카오 계정으로 로그인한 사용자를 처리함
    private SocialMemberResult handleExistingAccount(SocialAccount socialAccount, KakaoUserInfo userInfo) {
        Member member = socialAccount.getMember();

        if (!member.isActive()) {
            throw new CustomException(MemberErrorCode.MEMBER_INACTIVE);
        }

        // 카카오 이메일은 사용자가 변경할 수 있기 때문에 로그인할 때 최신 값으로 갱신
        socialAccount.updateProviderEmail(userInfo.email());

        return SocialMemberResult.existing(member);
    }

    // 신규 사용자와 해당 사용자의 카카오 소셜 계정을 생성함
    private SocialMemberResult createMemberAndSocialAccount(KakaoUserInfo userInfo) {
        String nickname = StringUtils.hasText(userInfo.nickname())
                ? userInfo.nickname()
                : DEFAULT_NICKNAME;

        Member member = Member.create(userInfo.email(), nickname);

        memberRepository.save(member);

        /**
         * 카카오 사용자 ID를 기준으로 소셜 계정 생성
         * providerUserId는 동일한 카카오 사용자를 식별하는 핵심 값
         */
        SocialAccount socialAccount = SocialAccount.create(
                member,
                SocialProvider.KAKAO,
                userInfo.providerUserId(),
                userInfo.email()
        );

        socialAccountRepository.save(socialAccount);

        return SocialMemberResult.created(member);
    }
}
