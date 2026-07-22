package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.dto.request.DevTokenRequest;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DevAuthService {

    private final MemberService memberService;
    private final AuthTokenService authTokenService;

    public DevTokenResult issueToken(DevTokenRequest request) {
        Member member = memberService.findOrCreateDeveloperMember(
                request.email(),
                request.nickname()
        );

        AuthTokenResult tokenResult = authTokenService.issueTokens(member);

        return new DevTokenResult(member.getId(), tokenResult);
    }

    public record DevTokenResult(
            Long memberId,
            AuthTokenResult tokenResult
    ) {
    }
}
