package com.umc.todayter.domain.notifications.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private boolean isTodayRemind;

    @Enumerated(EnumType.STRING)
    private RemindCycle remindCycle;

    @Enumerated(EnumType.STRING)
    private RemindTime remindTime;

    private boolean isSavedPlace;
    private boolean isServiceNotice;
    private boolean isMarketing;

    public void updateSettings(boolean isTodayRemind, RemindCycle remindCycle, RemindTime remindTime,
                               boolean isSavedPlace, boolean isServiceNotice, boolean isMarketing) {
        this.isTodayRemind = isTodayRemind;
        this.remindCycle = remindCycle;
        this.remindTime = remindTime;
        this.isSavedPlace = isSavedPlace;
        this.isServiceNotice = isServiceNotice;
        this.isMarketing = isMarketing;
    }
}