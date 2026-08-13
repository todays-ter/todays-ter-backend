package com.umc.todayter.domain.auth.client.apple;

import com.umc.todayter.domain.auth.client.apple.dto.AppleTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 해당 클라이언트는 다음 요청을 보냄
 *
 * POST https://appleid.apple.com/auth/token
 * Content-Type: application/x-www-form-urlencoded
 */
@FeignClient(
        name = "appleAuthClient",
        url = "${apple.oauth.auth-base-url}"
)
public interface AppleAuthClient {

    @PostMapping(
            value = "/auth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    AppleTokenResponse issueToken(
            @RequestBody MultiValueMap<String, String> form
    );
}
