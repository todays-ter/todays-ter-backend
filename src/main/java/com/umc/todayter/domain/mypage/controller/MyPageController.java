package com.umc.todayter.domain.mypage.controller;

import com.umc.todayter.domain.mypage.dto.MyPageRequestDTO;
import com.umc.todayter.domain.mypage.dto.MyPageResponseDTO;
import com.umc.todayter.domain.mypage.service.MyPageService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    // 1. 알림 설정 조회 (GET /mypage/notification-settings)
    @GetMapping("/notification-settings")
    public ApiResponse<MyPageResponseDTO.NotificationSettingDTO> getNotificationSettings() {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(myPageService.getNotificationSettings(userId), SuccessCode.OK);
    }

    // 2. 알림 설정 변경 (PATCH /mypage/notification-settings)
    @PatchMapping("/notification-settings")
    public ApiResponse<MyPageResponseDTO.UpdateResultDTO> updateNotificationSettings(
            @RequestBody MyPageRequestDTO.UpdateNotificationSettingDTO request) {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(myPageService.updateNotificationSettings(userId, request), SuccessCode.OK);
    }

    // 3. 권한 설정 조회 (GET /mypage/permissions)
    @GetMapping("/permissions")
    public ApiResponse<MyPageResponseDTO.PermissionDTO> getPermissions() {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(myPageService.getPermissions(userId), SuccessCode.OK);
    }

    // 4. 권한 설정 변경 (PATCH /mypage/permissions)
    @PatchMapping("/permissions")
    public ApiResponse<MyPageResponseDTO.UpdateResultDTO> updatePermissions(
            @RequestBody MyPageRequestDTO.UpdatePermissionDTO request) {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(myPageService.updatePermissions(userId, request), SuccessCode.OK);
    }

    // 5. 개인정보 및 약관 목록 조회 (GET /mypage/policies)
    @GetMapping("/policies")
    public ApiResponse<MyPageResponseDTO.PolicyListDTO> getPolicies() {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(myPageService.getPolicies(userId), SuccessCode.OK);
    }
}