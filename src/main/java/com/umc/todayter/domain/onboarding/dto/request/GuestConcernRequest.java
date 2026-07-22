package com.umc.todayter.domain.onboarding.dto.request;

import com.umc.todayter.domain.onboarding.enums.ConcernType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GuestConcernRequest(
        @NotEmpty(message = "고민 유형을 한 개 이상 선택해야 합니다.")
        List<@NotNull(message = "고민 유형에는 null을 포함할 수 없습니다.")
                ConcernType> concernTypes
) {
}
