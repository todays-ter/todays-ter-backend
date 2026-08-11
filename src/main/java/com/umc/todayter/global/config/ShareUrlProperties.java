package com.umc.todayter.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "share")
public record ShareUrlProperties(
        String frontendBaseUrl,
        String fortuneReportPath,
        String recommendedPlacePath
) {
}
