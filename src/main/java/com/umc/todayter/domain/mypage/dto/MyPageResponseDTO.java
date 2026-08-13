package com.umc.todayter.domain.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public final class MyPageResponseDTO {

    private MyPageResponseDTO() {
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "마이페이지 메인 조회 응답")
    public static class MainDTO {

        @Schema(description = "최신 완료 리포트 ID. 완료된 리포트가 없으면 null", example = "83721")
        private Long reportId;

        @Schema(description = "회원 닉네임", example = "오늘이")
        private String nickname;

        @Schema(description = "프로필 이미지 URL. 현재 미지원이면 null", example = "https://example.com/profile.jpg")
        private String profileImageUrl;

        @Schema(description = "회원 ID", example = "1")
        private Long userId;

        @Schema(description = "대표 소셜 로그인 제공자. 연결된 계정이 없으면 null", example = "KAKAO")
        private String loginProvider;

        @Schema(description = "주 오행. 완료된 리포트가 없으면 null", example = "수")
        private String mainElement;

        @Schema(description = "보완 오행. 완료된 리포트가 없으면 null", example = "화")
        private String complementaryElement;
    }
}
