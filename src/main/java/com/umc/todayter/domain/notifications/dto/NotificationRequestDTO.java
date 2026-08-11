package com.umc.todayter.domain.notifications.dto;

import com.umc.todayter.domain.notifications.entity.RemindCycle;
import com.umc.todayter.domain.notifications.entity.RemindTime;
import lombok.Getter;

public class NotificationRequestDTO {

    @Getter
    public static class UpdateNotificationSettingDTO {
        private boolean isTodayRemind;
        private RemindCycle remindCycle;
        private RemindTime remindTime;
        private boolean isSavedPlace;
        private boolean isServiceNotice;
        private boolean isMarketing;
    }
}