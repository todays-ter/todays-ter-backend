package com.umc.todayter.domain.auth.client.apple;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.todayter.domain.auth.client.apple.dto.ApplePublicKeyResponse;
import com.umc.todayter.domain.auth.dto.AppleUserInfo;
import com.umc.todayter.domain.auth.exception.AuthErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.config.properties.AppleOAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleIdentityTokenValidator {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final ApplePublicKeyClient applePublicKeyClient;
    private final AppleOAuthProperties properties;
    private final ObjectMapper objectMapper;

    public AppleUserInfo validate(String identityToken) {

        try {
            String kid = extractKid(identityToken);

            ApplePublicKeyResponse.Key appleKey = findMatchingKey(kid);

            PublicKey publicKey = buildPublicKey(appleKey);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(identityToken)
                    .getPayload();

            validateClaims(claims);

            String sub = claims.getSubject();
            String email = claims.get("email", String.class);

            if (!StringUtils.hasText(sub)) {
                throw new CustomException(AuthErrorCode.APPLE_USER_ID_MISSING);
            }

            return new AppleUserInfo(sub, email);

        } catch (CustomException e) {
            throw e;

        } catch (JwtException | IllegalArgumentException e) {

            log.warn(
                    "Apple Identity Token 검증 실패: {}",
                    e.getMessage()
            );

            throw new CustomException(AuthErrorCode.APPLE_IDENTITY_TOKEN_INVALID);

        } catch (Exception e) {

            log.warn(
                    "Apple Identity Token 처리 실패: {}",
                    e.getMessage()
            );

            throw new CustomException(AuthErrorCode.APPLE_IDENTITY_TOKEN_INVALID);
        }
    }

    private String extractKid(String identityToken) {

        try {
            String[] parts = identityToken.split("\\.");

            if (parts.length != 3) {
                throw new CustomException(AuthErrorCode.APPLE_IDENTITY_TOKEN_INVALID);
            }

            byte[] headerBytes = Base64.getUrlDecoder().decode(parts[0]);

            Map<String, Object> header = objectMapper.readValue(
                    headerBytes,
                    new TypeReference<>() {}
            );

            Object kid = header.get("kid");

            if (!(kid instanceof String kidValue) || !StringUtils.hasText(kidValue)) {
                throw new CustomException(AuthErrorCode.APPLE_IDENTITY_TOKEN_INVALID);
            }

            return kidValue;

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            throw new CustomException(AuthErrorCode.APPLE_IDENTITY_TOKEN_INVALID);
        }
    }

    private ApplePublicKeyResponse.Key findMatchingKey(String kid) {

        ApplePublicKeyResponse response = applePublicKeyClient.getPublicKeys();

        if (response == null || response.keys() == null) {
            throw new CustomException(AuthErrorCode.APPLE_PUBLIC_KEY_FAILED);
        }

        return response.keys().stream()
                .filter(key -> kid.equals(key.kid()) && "RSA".equals(key.kty()) && "RS256".equals(key.alg()))
                .findFirst()
                .orElseThrow(() -> new CustomException(AuthErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND));
    }

    private PublicKey buildPublicKey(ApplePublicKeyResponse.Key key) {

        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(key.n());

            byte[] exponentBytes = Base64.getUrlDecoder().decode(key.e());

            BigInteger modulus = new BigInteger(1, modulusBytes);

            BigInteger exponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);

            return KeyFactory
                    .getInstance("RSA")
                    .generatePublic(keySpec);

        } catch (Exception e) {

            throw new CustomException(AuthErrorCode.APPLE_PUBLIC_KEY_FAILED);
        }
    }

    private void validateClaims(Claims claims) {

        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new CustomException(AuthErrorCode.APPLE_IDENTITY_TOKEN_INVALID);
        }

        if (!claims.getAudience().contains(properties.clientId())) {

            throw new CustomException(AuthErrorCode.APPLE_IDENTITY_TOKEN_INVALID);
        }
    }
}
