package com.umc.todayter.domain.mypage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.todayter.domain.mypage.entity.PolicyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MyPageResponseDTO {

    // 0. GET /mypage (마이페이지 메인 조회) 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "마이페이지 메인 조회 응답 DTO")
    public static class MainDTO {

        @Schema(description = "리포트 ID", example = "83721")
        private Long reportId;

        @Schema(description = "유저 닉네임", example = "사용자닉네임")
        private String nickname;

        @Schema(description = "프로필 이미지 URL (미등록 시 null)", example = "https://example.com/profile.jpg")
        private String profileImageUrl;

        @Schema(description = "유저 고유 식별자(PK)", example = "1")
        private Long userId;

        @Schema(description = "현재 로그인된 소셜 계정 타입 (KAKAO, NAVER, APPLE, EMAIL)", example = "KAKAO")
        private String loginProvider;

        @Schema(description = "주 오행", example = "수")
        private String mainElement;

        @Schema(description = "보완 오행", example = "화")
        private String complementaryElement;
    }

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