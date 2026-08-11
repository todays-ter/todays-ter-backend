package com.umc.todayter.domain.member.dto.request;

import com.umc.todayter.domain.onboarding.enums.ConcernType;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MemberConcernUpdateRequest(

        @NotEmpty
        List<ConcernType> concernTypes
) {
}
