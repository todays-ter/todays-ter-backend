package com.umc.todayter.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple.oauth")
public record AppleOAuthProperties(
        String clientId,
        String teamId,
        String keyId,
        String privateKey,
        String redirectUri,
        String authBaseUrl
) {
}
