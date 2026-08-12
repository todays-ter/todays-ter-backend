package com.umc.todayter.domain.mypage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.todayter.domain.mypage.entity.PolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MyPageResponseDTO {

    // GET /mypage/notification-settings 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettingDTO {
        private boolean isPushEnabled;
        private boolean isMarketingEnabled;
        private boolean isNightMarketingEnabled;
    }

    // PATCH 변경 성공 공통 응답 DTO (updatedAt)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateResultDTO {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }

    // GET /mypage/permissions 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionDTO {
        private boolean isCameraAllowed;
        private boolean isPhotoLibraryAllowed;
        private boolean isLocationAllowed;
    }

    // GET /mypage/policies 단일 약관 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyDTO {
        private PolicyType type;
        private String title;
        private String url;
        private boolean isRequired;
        private boolean isAgreed;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime agreedAt;
    }

    // GET /mypage/policies 전체 목록 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyListDTO {
        private List<PolicyDTO> policies;
    }
}