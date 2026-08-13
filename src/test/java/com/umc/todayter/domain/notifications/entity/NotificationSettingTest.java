package com.umc.todayter.domain.notifications.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationSettingTest {

    @Test
    void defaultSettingUsesDesignDefaults() {
        NotificationSetting setting = NotificationSetting.createDefault(1L);

        assertThat(setting.isTodayRemind()).isTrue();
        assertThat(setting.getRemindCycle()).isEqualTo(RemindCycle.EVERY_2_DAYS);
        assertThat(setting.getRemindTime()).isEqualTo(RemindTime.SIX_PM);
        assertThat(setting.isSavedPlace()).isTrue();
        assertThat(setting.isServiceNotice()).isTrue();
        assertThat(setting.isMarketing()).isTrue();
    }

    @Test
    void partialUpdateKeepsFieldsThatWereNotSent() {
        NotificationSetting setting = NotificationSetting.createDefault(1L);

        setting.updateSettings(null, null, null, null, null, false);

        assertThat(setting.isTodayRemind()).isTrue();
        assertThat(setting.getRemindCycle()).isEqualTo(RemindCycle.EVERY_2_DAYS);
        assertThat(setting.getRemindTime()).isEqualTo(RemindTime.SIX_PM);
        assertThat(setting.isSavedPlace()).isTrue();
        assertThat(setting.isServiceNotice()).isTrue();
        assertThat(setting.isMarketing()).isFalse();
    }

    @Test
    void legacyNullCycleAndTimeAreRepaired() throws ReflectiveOperationException {
        NotificationSetting setting = NotificationSetting.createDefault(1L);
        setField(setting, "remindCycle", null);
        setField(setting, "remindTime", null);

        setting.repairLegacyDefaults();

        assertThat(setting.getRemindCycle()).isEqualTo(RemindCycle.EVERY_2_DAYS);
        assertThat(setting.getRemindTime()).isEqualTo(RemindTime.SIX_PM);
    }

    @Test
    void twoDayCycleBecomesDueAfterTwoDays() {
        NotificationSetting setting = NotificationSetting.createDefault(1L);
        LocalDate firstDate = LocalDate.of(2026, 8, 13);

        setting.markTodayReminderSent(firstDate);

        assertThat(setting.isTodayReminderDue(firstDate.plusDays(1))).isFalse();
        assertThat(setting.isTodayReminderDue(firstDate.plusDays(2))).isTrue();
    }

    private void setField(NotificationSetting setting, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = NotificationSetting.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(setting, value);
    }
}
