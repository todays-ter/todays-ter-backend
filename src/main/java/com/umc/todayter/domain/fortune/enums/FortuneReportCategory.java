package com.umc.todayter.domain.fortune.enums;

import java.util.Locale;

public enum FortuneReportCategory {
    GENERAL,
    LOVE,
    CAREER,
    WEALTH,
    RELATIONSHIP,
    HEALTH;

    public static FortuneReportCategory from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
