package com.umc.todayter.domain.mypage.controller;

import com.umc.todayter.domain.mypage.dto.MyPageResponseDTO;
import com.umc.todayter.domain.mypage.service.MyPageService;
import com.umc.todayter.domain.member.dto.response.SocialAccountListResponse;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mypage", description = "마이페이지 관련 API")
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "마이페이지 메인 조회 API", description = "로그인한 회원의 닉네임과 최신 완료 리포트의 오행 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<MyPageResponseDTO.MainDTO> getMyPage() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(myPageService.getMyPage(memberId), SuccessCode.OK);
    }

    @Operation(summary = "계정 연동 관리 조회 API", description = "로그인한 유저에게 연결된 소셜 계정 목록을 조회합니다.")
    @GetMapping("/social-connections")
    public ApiResponse<SocialAccountListResponse> getSocialConnections() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(myPageService.getSocialConnections(memberId), SuccessCode.OK);
    }

}
