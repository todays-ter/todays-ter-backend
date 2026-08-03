package com.umc.todayter.global.config.ablecityProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fortune-report")
public record FortuneReportProperties(int maxRetries) {
}
