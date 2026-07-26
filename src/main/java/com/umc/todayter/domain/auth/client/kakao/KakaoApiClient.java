package com.umc.todayter.domain.auth.client.kakao;

import com.umc.todayter.domain.auth.client.kakao.dto.KakaoUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * GET https://kapi.kakao.com/v2/user/me
 * Authorization: Bearer {KAKAO_ACCESS_TOKEN}
 */
@FeignClient(
        name = "kakaoApiClient",
        url = "${kakao.oauth.api-base-url}"
)
public interface KakaoApiClient {

    @GetMapping("/v2/user/me")
    KakaoUserResponse getUserInformation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
