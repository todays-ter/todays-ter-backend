package com.umc.todayter.domain.auth.client.kakao;

import com.umc.todayter.domain.auth.client.kakao.dto.KakaoTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 해당 클라이언트는 다음 요청을 보냄
 *
 * POST https://kauth.kakao.com/oauth/token
 * Content-Type: application/x-www-form-urlencoded
 */
@FeignClient(
        name = "kakaoAuthClient",
        url = "${kakao.oauth.auth-base-url}"
)
public interface KakaoAuthClient {

    @PostMapping(
            value = "/oauth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    KakaoTokenResponse issueToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String authorizationCode,
            @RequestParam("client_secret") String clientSecret
    );
}
