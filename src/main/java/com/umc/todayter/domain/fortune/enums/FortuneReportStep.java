package com.umc.todayter.domain.fortune.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FortuneReportStep {
    WAITING,
    BIRTH_DATA_PREPARED,
    MANSE_DATA_CREATED,
    PROMPT_PREPARED,
    AI_REPORT_CREATED,
    COMPLETED,
    FAILED;

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }
}
