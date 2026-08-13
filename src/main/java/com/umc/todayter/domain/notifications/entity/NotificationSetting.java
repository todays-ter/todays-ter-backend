package com.umc.todayter.domain.notifications.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_setting_user_id",
                columnNames = "user_id"
        )
)
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_today_remind", nullable = false)
    private boolean isTodayRemind;

    @Enumerated(EnumType.STRING)
    @Column(name = "remind_cycle", length = 30)
    private RemindCycle remindCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "remind_time", length = 30)
    private RemindTime remindTime;

    @Column(name = "is_saved_place", nullable = false)
    private boolean isSavedPlace;

    @Column(name = "is_service_notice", nullable = false)
    private boolean isServiceNotice;

    @Column(name = "is_marketing", nullable = false)
    private boolean isMarketing;

    @Column(name = "last_today_remind_date")
    private LocalDate lastTodayRemindDate;

    @Column(name = "last_saved_place_notice_date")
    private LocalDate lastSavedPlaceNoticeDate;

    private NotificationSetting(Long userId) {
        this.userId = userId;
        this.isTodayRemind = true;
        this.remindCycle = RemindCycle.EVERY_2_DAYS;
        this.remindTime = RemindTime.SIX_PM;
        this.isSavedPlace = true;
        this.isServiceNotice = true;
        this.isMarketing = true;
    }

    public static NotificationSetting createDefault(Long userId) {
        return new NotificationSetting(userId);
    }

    public void updateSettings(
            Boolean todayRemind,
            RemindCycle remindCycle,
            RemindTime remindTime,
            Boolean savedPlace,
            Boolean serviceNotice,
            Boolean marketing
    ) {
        if (todayRemind != null) {
            this.isTodayRemind = todayRemind;
        }
        if (remindCycle != null) {
            this.remindCycle = remindCycle;
        }
        if (remindTime != null) {
            this.remindTime = remindTime;
        }
        if (savedPlace != null) {
            this.isSavedPlace = savedPlace;
        }
        if (serviceNotice != null) {
            this.isServiceNotice = serviceNotice;
        }
        if (marketing != null) {
            this.isMarketing = marketing;
        }
    }

    public void repairLegacyDefaults() {
        if (remindCycle == null) {
            remindCycle = RemindCycle.EVERY_2_DAYS;
        }
        if (remindTime == null) {
            remindTime = RemindTime.SIX_PM;
        }
    }

    public boolean isTodayReminderDue(LocalDate date) {
        return isTodayRemind && isCycleDue(lastTodayRemindDate, date);
    }

    public boolean isSavedPlaceNoticeDue(LocalDate date) {
        return isSavedPlace && isCycleDue(lastSavedPlaceNoticeDate, date);
    }

    public void markTodayReminderSent(LocalDate date) {
        this.lastTodayRemindDate = date;
    }

    public void markSavedPlaceNoticeSent(LocalDate date) {
        this.lastSavedPlaceNoticeDate = date;
    }

    private boolean isCycleDue(LocalDate lastSentDate, LocalDate date) {
        repairLegacyDefaults();
        return lastSentDate == null || !lastSentDate.plusDays(remindCycle.getIntervalDays()).isAfter(date);
    }
}
