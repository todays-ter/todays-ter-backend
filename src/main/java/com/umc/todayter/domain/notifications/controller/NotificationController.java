package com.umc.todayter.domain.notifications.controller;

import com.umc.todayter.domain.notifications.dto.NotificationRequestDTO;
import com.umc.todayter.domain.notifications.dto.NotificationResponseDTO;
import com.umc.todayter.domain.notifications.service.NotificationService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 목록, 읽음 처리 및 알림 설정 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회")
    @GetMapping
    public ApiResponse<NotificationResponseDTO.NotificationListResultDTO> getNotifications(
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 10, message = "size는 10 이하여야 합니다.")
            int size,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(
                notificationService.getNotifications(memberId, cursor, size),
                SuccessCode.OK
        );
    }

    @Operation(summary = "미읽음 알림 개수 조회")
    @GetMapping("/unread-count")
    public ApiResponse<NotificationResponseDTO.UnreadCountResultDTO> getUnreadCount() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(notificationService.getUnreadCount(memberId), SuccessCode.OK);
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponseDTO.ReadResultDTO> readNotification(
            @PathVariable(name = "notificationId")
            @Positive(message = "notificationId는 1 이상이어야 합니다.")
            Long notificationId
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(
                notificationService.readNotification(memberId, notificationId),
                SuccessCode.OK
        );
    }

    @Operation(summary = "알림 전체 읽음 처리")
    @PatchMapping("/read-all")
    public ApiResponse<NotificationResponseDTO.ReadAllResultDTO> readAllNotifications() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(notificationService.readAllNotifications(memberId), SuccessCode.OK);
    }

    @Operation(summary = "알림 설정 조회")
    @GetMapping("/settings")
    public ApiResponse<NotificationResponseDTO.NotificationSettingDTO> getNotificationSettings() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(notificationService.getNotificationSettings(memberId), SuccessCode.OK);
    }

    @Hidden
    @GetMapping("/status")
    public ApiResponse<NotificationResponseDTO.NotificationSettingDTO> getLegacyNotificationSettings() {
        return getNotificationSettings();
    }

    @Operation(summary = "알림 설정 수정", description = "변경할 설정 필드만 전송할 수 있습니다.")
    @PatchMapping("/settings")
    public ApiResponse<NotificationResponseDTO.NotificationSettingDTO> updateNotificationSettings(
            @Valid @RequestBody NotificationRequestDTO.UpdateNotificationSettingDTO request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(
                notificationService.updateNotificationSettings(memberId, request),
                SuccessCode.OK
        );
    }
}
