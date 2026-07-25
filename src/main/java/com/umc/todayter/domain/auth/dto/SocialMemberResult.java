package com.umc.todayter.domain.auth.dto;

import com.umc.todayter.domain.member.entity.Member;

public record SocialMemberResult(
        Member member,
        boolean newMember
) {
    public static SocialMemberResult existing(Member member) {
        return new SocialMemberResult(member, false);
    }

    public static SocialMemberResult created(Member member) {
        return new SocialMemberResult(member, true);
    }
}
