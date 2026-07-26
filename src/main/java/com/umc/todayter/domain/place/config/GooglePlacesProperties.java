package com.umc.todayter.domain.place.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "google.places")
public record GooglePlacesProperties(
        String baseUrl,
        String apiKey,
        int thumbnailMaxWidth,
        Duration connectTimeout,
        Duration readTimeout
) {
}
