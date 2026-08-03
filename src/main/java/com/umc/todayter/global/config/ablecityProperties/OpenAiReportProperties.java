package com.umc.todayter.global.config.ablecityProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "fortune-report.openai")
public record OpenAiReportProperties(
        String baseUrl,
        String apiKey,
        String model,
        String promptResource,
        Duration connectTimeout,
        Duration readTimeout
) {
}
