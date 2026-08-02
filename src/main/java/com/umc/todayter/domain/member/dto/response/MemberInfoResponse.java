package com.umc.todayter.domain.member.dto.response;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;

public record MemberInfoResponse(
        Long memberId,
        String email,
        String nickname,
        MemberStatus status
) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getStatus()
        );
    }
}
