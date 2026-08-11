package com.umc.todayter.domain.notifications.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.todayter.domain.notifications.entity.NotificationType;
import com.umc.todayter.domain.notifications.entity.RemindCycle;
import com.umc.todayter.domain.notifications.entity.RemindTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDTO {
        private Long notificationId;
        private String title;
        private String content;
        private NotificationType type;
        private boolean isRead;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationListResultDTO {
        private List<NotificationDTO> notificationList;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadCountResultDTO {
        private int unreadCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadResultDTO {
        private Long notificationId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadAllResultDTO {
        private int updatedCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettingDTO {
        private boolean isTodayRemind;
        private RemindCycle remindCycle;
        private RemindTime remindTime;
        private boolean isSavedPlace;
        private boolean isServiceNotice;
        private boolean isMarketing;
    }
}