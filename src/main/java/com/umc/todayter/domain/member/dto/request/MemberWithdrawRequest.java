package com.umc.todayter.domain.member.dto.request;

import com.umc.todayter.domain.member.enums.WithdrawReason;
import jakarta.validation.constraints.NotNull;

public record MemberWithdrawRequest(
        @NotNull(message = "탈퇴 사유는 필수입니다.")
        WithdrawReason withdrawReason
) {
}
