package com.umc.todayter.domain.member.service;

import com.umc.todayter.domain.member.dto.response.SocialAccountListResponse;
import com.umc.todayter.domain.member.entity.SocialAccount;
import com.umc.todayter.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAccountQueryService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberService memberService;

    public SocialAccountListResponse getSocialAccounts(Long memberId) {
        memberService.getActiveMember(memberId);

        List<SocialAccount> socialAccounts = socialAccountRepository.findAllByMemberIdOrderByIdAsc(memberId);

        return SocialAccountListResponse.from(socialAccounts);
    }
}
