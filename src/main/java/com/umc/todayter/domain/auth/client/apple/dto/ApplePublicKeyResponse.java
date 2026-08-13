package com.umc.todayter.domain.auth.client.apple.dto;

import java.util.List;

public record ApplePublicKeyResponse(
        List<Key> keys
) {
    public record Key(
            String kty,
            String kid,
            String use,
            String alg,
            String n,
            String e
    ) {
    }
}
