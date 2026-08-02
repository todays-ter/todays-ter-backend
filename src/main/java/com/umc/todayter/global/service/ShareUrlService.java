package com.umc.todayter.global.service;

import com.umc.todayter.global.config.ShareUrlProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ShareUrlService {

    private final ShareUrlProperties properties;

    public String fortuneReportUrl(String shareToken) {
        return build(properties.fortuneReportPath(), shareToken);
    }

    public String recommendedPlaceUrl(Long placeId) {
        return build(properties.recommendedPlacePath(), placeId);
    }

    private String build(String path, Object pathVariable) {
        return UriComponentsBuilder
                .fromUriString(properties.frontendBaseUrl())
                .path(path)
                .buildAndExpand(pathVariable)
                .toUriString();
    }
}
