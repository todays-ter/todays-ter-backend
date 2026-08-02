package com.umc.todayter.domain.fortune.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneReportCategoryTest {

    @Test
    void parsesCategoryRegardlessOfCase() {
        assertThat(FortuneReportCategory.from("GENERAL")).isEqualTo(FortuneReportCategory.GENERAL);
        assertThat(FortuneReportCategory.from("general")).isEqualTo(FortuneReportCategory.GENERAL);
        assertThat(FortuneReportCategory.from(" Career ")).isEqualTo(FortuneReportCategory.CAREER);
        assertThat(FortuneReportCategory.from("unknown")).isNull();
    }
}
