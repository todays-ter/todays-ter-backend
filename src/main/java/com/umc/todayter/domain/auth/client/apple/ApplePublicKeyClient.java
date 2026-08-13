package com.umc.todayter.domain.auth.client.apple;

import com.umc.todayter.domain.auth.client.apple.dto.ApplePublicKeyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "applePublicKeyClient",
        url = "${apple.oauth.auth-base-url}"
)
public interface ApplePublicKeyClient {

    @GetMapping("/auth/keys")
    ApplePublicKeyResponse getPublicKeys();
}
