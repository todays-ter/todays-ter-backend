package com.umc.todayter.domain.notifications.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RemindCycle {
    EVERY_DAY("EVERY_DAY"),       // 매일
    EVERY_2_DAYS("EVERY_2_DAYS"), // 2일마다
    EVERY_3_DAYS("EVERY_3_DAYS"), // 3일마다
    EVERY_WEEK("EVERY_WEEK");     // 매주

    @JsonValue
    private final String value;

    @JsonCreator
    public static RemindCycle fromValue(String value) {
        for (RemindCycle cycle : RemindCycle.values()) {
            if (cycle.value.equalsIgnoreCase(value) || cycle.name().equalsIgnoreCase(value)) {
                return cycle;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 알림 주기입니다: " + value);
    }
}