package com.umc.todayter.domain.auth.client.apple;

import com.umc.todayter.domain.auth.exception.AuthErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.config.properties.AppleOAuthProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AppleClientSecretGenerator {

    private static final long CLIENT_SECRET_EXPIRE_SECONDS = 300L;

    private final AppleOAuthProperties properties;

    public String generate() {
        try {
            PrivateKey privateKey = buildPrivateKey();

            Instant now = Instant.now();

            return Jwts.builder()
                    .header()
                    .keyId(properties.keyId())
                    .and()
                    .issuer(properties.teamId())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(
                            now.plusSeconds(CLIENT_SECRET_EXPIRE_SECONDS)
                    ))
                    .audience()
                    .add(properties.authBaseUrl())
                    .and()
                    .subject(properties.clientId())
                    .signWith(privateKey, Jwts.SIG.ES256)
                    .compact();

        } catch (Exception e) {
            throw new CustomException(AuthErrorCode.APPLE_CLIENT_SECRET_GENERATION_FAILED);
        }
    }

    private PrivateKey buildPrivateKey() throws Exception {

        String privateKeyValue = properties.privateKey()
                .replace("\\n", "\n")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyValue);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        return KeyFactory.getInstance("EC").generatePrivate(keySpec);
    }
}
