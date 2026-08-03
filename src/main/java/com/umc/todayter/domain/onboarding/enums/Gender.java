package com.umc.todayter.domain.onboarding.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum Gender {
    MALE,
    FEMALE;

    @JsonCreator
    public static Gender from(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Gender.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("성별은 male 또는 female이어야 합니다.", e);
        }
    }

    public String toAblecityValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
