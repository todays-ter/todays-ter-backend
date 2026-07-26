package com.umc.todayter.domain.fortune.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fortune-report")
public record FortuneReportProperties(int maxRetries) {
}
