package com.umc.todayter.global.config.ablecityProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "fortune-report.ablecity")
public record AblecityApiProperties(
        String baseUrl,
        String apiKey,
        Duration connectTimeout,
        Duration readTimeout
) {
    @Override
    public String toString() {
        return "AblecityApiProperties[baseUrl=" + baseUrl
                + ", apiKey=[REDACTED], connectTimeout=" + connectTimeout
                + ", readTimeout=" + readTimeout + "]";
    }
}
