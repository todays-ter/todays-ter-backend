package com.umc.todayter.domain.member.controller;

import com.umc.todayter.domain.member.dto.request.MemberWithdrawRequest;
import com.umc.todayter.domain.member.dto.response.MemberSajuResponse;
import com.umc.todayter.domain.member.enums.code.MemberSuccessCode;
import com.umc.todayter.domain.member.service.MemberSajuService;
import com.umc.todayter.domain.member.service.MemberWithdrawService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.security.SecurityUtil;
import com.umc.todayter.global.util.AuthCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberSajuService memberSajuService;
    private final MemberWithdrawService memberWithdrawService;
    private final AuthCookieUtil authCookieUtil;

    @Operation(
            summary = "회원 사주 정보 조회",
            description = """
                현재 로그인한 회원에게 연결된 사주 정보를 조회합니다.
                비회원 온보딩에서 회원 계정으로 이전된 사주 정보도 동일하게 조회됩니다.
                """
    )
    @GetMapping("/me/saju")
    public ResponseEntity<ApiResponse<MemberSajuResponse>> getSaju() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberSajuResponse result = memberSajuService.getSaju(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, MemberSuccessCode.SAJU_RETRIEVED));
    }

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    현재 로그인한 회원을 WITHDRAWN 상태로 변경하고
                    탈퇴 사유 및 탈퇴 일시를 저장합니다.
                    Refresh Token 정보를 제거하고 인증 쿠키를 삭제합니다.
                    """
    )
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @Valid @RequestBody MemberWithdrawRequest request,
            HttpServletResponse response
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        memberWithdrawService.withdraw(memberId, request.withdrawReason());
        authCookieUtil.clearRefreshTokenCookie(response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(null, MemberSuccessCode.MEMBER_WITHDRAWN));
    }
}
