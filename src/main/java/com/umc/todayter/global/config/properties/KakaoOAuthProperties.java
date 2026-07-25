package com.umc.todayter.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.oauth")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String authBaseUrl,
        String apiBaseUrl
) {
}
