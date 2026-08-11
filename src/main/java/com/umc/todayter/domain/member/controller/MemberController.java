package com.umc.todayter.domain.member.controller;

import com.umc.todayter.domain.member.dto.request.MemberSajuUpdateRequest;
import com.umc.todayter.domain.member.dto.request.MemberWithdrawRequest;
import com.umc.todayter.domain.member.dto.response.MemberConcernResponse;
import com.umc.todayter.domain.member.dto.response.MemberInfoResponse;
import com.umc.todayter.domain.member.dto.response.MemberSajuResponse;
import com.umc.todayter.domain.member.dto.response.SocialAccountListResponse;
import com.umc.todayter.domain.member.enums.code.MemberSuccessCode;
import com.umc.todayter.domain.member.service.*;
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
    private final MemberConcernService memberConcernService;
    private final MemberService memberService;
    private final SocialAccountQueryService socialAccountQueryService;
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
            summary = "회원 사주 정보 수정",
            description = """
                현재 로그인한 회원의 사주 정보를 수정합니다.
                기존 방문 및 저장 기록은 유지되며,
                이후 생성되는 추천과 리포트부터 변경된 정보가 적용됩니다.
                """
    )
    @PutMapping("/me/saju")
    public ResponseEntity<ApiResponse<MemberSajuResponse>> updateSaju(
            @Valid @RequestBody MemberSajuUpdateRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberSajuResponse result = memberSajuService.updateSaju(memberId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, MemberSuccessCode.SAJU_UPDATED));
    }

    @Operation(
            summary = "회원 고민 유형 조회",
            description = """
            현재 로그인한 회원에게 연결된 고민 유형을 조회합니다.
            비회원 온보딩에서 회원 계정으로 이전된 고민 정보도 동일하게 조회됩니다.
            """
    )
    @GetMapping("/me/concerns")
    public ResponseEntity<ApiResponse<MemberConcernResponse>> getConcerns() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberConcernResponse result = memberConcernService.getConcerns(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, MemberSuccessCode.CONCERNS_RETRIEVED));
    }

    @Operation(
            summary = "내 정보 조회",
            description = """
                현재 로그인한 회원의 기본 정보를 조회합니다.
                Access Token에서 회원 ID를 추출하고,
                ACTIVE 상태인 회원의 이메일, 닉네임 및 상태를 반환합니다.
                """
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberInfoResponse result = memberService.getMemberInfo(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, MemberSuccessCode.MEMBER_INFO_RETRIEVED));
    }

    @Operation(
            summary = "연결된 소셜 계정 목록 조회",
            description = """
                현재 로그인한 회원에게 연결된 소셜 계정 목록을 조회합니다.
                현재 회원이 사용할 수 있는 소셜 로그인 수단과 계정 이메일을 반환합니다.
                """
    )
    @GetMapping("/me/social-accounts")
    public ResponseEntity<ApiResponse<SocialAccountListResponse>> getSocialAccounts() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        SocialAccountListResponse result = socialAccountQueryService.getSocialAccounts(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, MemberSuccessCode.SOCIAL_ACCOUNTS_RETRIEVED));
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
