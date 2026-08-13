package com.umc.todayter.domain.notifications.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RemindCycle {
    EVERY_DAY("EVERY_DAY", 1),
    EVERY_2_DAYS("EVERY_2_DAYS", 2),
    EVERY_3_DAYS("EVERY_3_DAYS", 3),
    EVERY_WEEK("EVERY_WEEK", 7);

    @JsonValue
    private final String value;
    private final int intervalDays;

    @JsonCreator
    public static RemindCycle fromValue(String value) {
        if ("EVERY_7_DAYS".equalsIgnoreCase(value)) {
            return EVERY_WEEK;
        }
        for (RemindCycle cycle : values()) {
            if (cycle.value.equalsIgnoreCase(value) || cycle.name().equalsIgnoreCase(value)) {
                return cycle;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 알림 주기입니다: " + value);
    }
}
