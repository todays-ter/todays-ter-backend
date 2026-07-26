package com.umc.todayter.domain.fortune.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FortuneReportStatus {
    PROCESSING,
    COMPLETED,
    FAILED;

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }
}
