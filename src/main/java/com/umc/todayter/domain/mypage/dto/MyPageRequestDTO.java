package com.umc.todayter.domain.mypage.dto;

import lombok.Getter;

public class MyPageRequestDTO {

    // PATCH /mypage/notification-settings 요청 DTO
    @Getter
    public static class UpdateNotificationSettingDTO {
        private boolean isPushEnabled;
        private boolean isMarketingEnabled;
        private boolean isNightMarketingEnabled;
    }

    // PATCH /mypage/permissions 요청 DTO
    @Getter
    public static class UpdatePermissionDTO {
        private boolean isCameraAllowed;
        private boolean isPhotoLibraryAllowed;
        private boolean isLocationAllowed;
    }
}