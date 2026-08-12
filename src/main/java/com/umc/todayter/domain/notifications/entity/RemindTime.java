package com.umc.todayter.domain.notifications.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RemindTime {
    NINE_AM("09:00"),       // 오전 9시
    TWELVE_PM("12:00"),     // 오후 12시
    SIX_PM("18:00"),        // 오후 6시
    NINE_PM("21:00");       // 오후 9시

    @JsonValue
    private final String value;

    @JsonCreator
    public static RemindTime fromValue(String value) {
        for (RemindTime time : RemindTime.values()) {
            if (time.value.equals(value)) {
                return time;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 알림 시간입니다: " + value);
    }
}