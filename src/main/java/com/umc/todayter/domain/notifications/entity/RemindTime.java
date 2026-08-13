package com.umc.todayter.domain.notifications.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public enum RemindTime {
    NINE_AM("09:00", LocalTime.of(9, 0)),
    TWELVE_PM("12:00", LocalTime.of(12, 0)),
    SIX_PM("18:00", LocalTime.of(18, 0)),
    NINE_PM("21:00", LocalTime.of(21, 0));

    @JsonValue
    private final String value;
    private final LocalTime time;

    @JsonCreator
    public static RemindTime fromValue(String value) {
        for (RemindTime remindTime : values()) {
            if (remindTime.value.equals(value) || remindTime.name().equalsIgnoreCase(value)) {
                return remindTime;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 알림 시간입니다: " + value);
    }
}
