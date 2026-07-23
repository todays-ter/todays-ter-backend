package com.umc.todayter.global.security.jwt;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Refresh Token 원문을 DB에 그대로 저장하지 않고, SHA-256 해시값으로 변환하는 역할을 하는 클래스
 *
 * Refresh Token 원문
 * -> SHA-256 해시
 * -> 64자리 16진수 문자열
 * -> DB 저장
 *
 * (Refresh Token 문자열을 SHA-256으로 해싱)
 */
@Component
public class RefreshTokenHasher {

    public String hash(String refreshToken) {
        try {
            // MessageDigest는 Java에서 해시 알고리즘을 사용할 때 제공되는 표준 클래스
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 문자열 -> 바이트 배열 -> 해싱
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));

            // 16진수 문자열로 변환
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 해시 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
