package com.umc.todayter.domain.notifications.controller;

import com.umc.todayter.domain.notifications.dto.NotificationRequestDTO;
import com.umc.todayter.domain.notifications.dto.NotificationResponseDTO;
import com.umc.todayter.domain.notifications.service.NotificationService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "알림 API", description = "알림 목록 조회 및 읽음 처리 관련 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1. 알림 목록 조회 (커서 페이징: default size=10)
    @Operation(summary = "알림 목록 조회")
    @GetMapping
    public ApiResponse<NotificationResponseDTO.NotificationListResultDTO> getNotifications(
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) Long cursor) {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(notificationService.getNotifications(userId, cursor, size), SuccessCode.OK);
    }

    // 2. 미읽음 알림 개수 조회 (홈 빨간 점 체크)
    @Operation(summary = "미읽음 알림 개수 조회")
    @GetMapping("/unread-count")
    public ApiResponse<NotificationResponseDTO.UnreadCountResultDTO> getUnreadCount() {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(notificationService.getUnreadCount(userId), SuccessCode.OK);
    }

    // 3. 알림 단건 읽음 처리
    
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponseDTO.ReadResultDTO> readNotification(
            @PathVariable(name = "notificationId") Long notificationId) {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(notificationService.readNotification(userId, notificationId), SuccessCode.OK);
    }

    // 4. 알림 전체 읽음 처리
    @Operation(summary = "알림 전체 읽음 처리")
    @PatchMapping("/read-all")
    public ApiResponse<NotificationResponseDTO.ReadAllResultDTO> readAllNotifications() {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(notificationService.readAllNotifications(userId), SuccessCode.OK);
    }

    // 5. 알림 설정 상태 조회
    @Operation(summary = "알림 설정 상태 조회")
    @GetMapping("/status")
    public ApiResponse<NotificationResponseDTO.NotificationSettingDTO> getNotificationSettings() {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(notificationService.getNotificationSettings(userId), SuccessCode.OK);
    }

    // 6. 알림 설정 수정
    @Operation(summary = "알림 설정 수정")
    @PatchMapping("/settings")
    public ApiResponse<NotificationResponseDTO.NotificationSettingDTO> updateNotificationSettings(
            @RequestBody NotificationRequestDTO.UpdateNotificationSettingDTO request) {
        Long userId = 1L; // TODO: JWT 토큰 추출 유저 ID
        return ApiResponse.onSuccess(notificationService.updateNotificationSettings(userId, request), SuccessCode.OK);
    }
}