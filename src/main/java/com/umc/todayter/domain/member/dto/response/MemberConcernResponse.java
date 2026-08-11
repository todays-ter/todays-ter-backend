package com.umc.todayter.domain.member.dto.response;

import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.ConcernType;

import java.util.List;

public record MemberConcernResponse(
        List<ConcernType> concernTypes
) {
    public static MemberConcernResponse from(Onboarding onboarding) {
        return new MemberConcernResponse(List.copyOf(onboarding.getConcernTypes()));
    }
}
