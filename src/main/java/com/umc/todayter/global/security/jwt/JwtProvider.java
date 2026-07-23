package com.umc.todayter.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

// JWT 토큰을 만들고, 검증하고, 안의 값을 꺼내는 클래스
@Component
public class JwtProvider {

    private final SecretKey secretKey; // JWT 서명 시 사용하는 비밀 키
    private final long accessExpireMs; // Access Token 유효시간
    private final long refreshExpireMs; // Refresh Token 유효시간

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expire}") long accessExpireMs,
            @Value("${jwt.refresh-expire}") long refreshExpireMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpireMs = accessExpireMs;
        this.refreshExpireMs = refreshExpireMs;
    }

    // Access Token 생성
    public String createAccessToken(Long memberId) {
        return createToken(memberId, "access", accessExpireMs);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long memberId) {
        return createToken(memberId, "refresh", refreshExpireMs);
    }

    // 실제 JWT 생성
    private String createToken(Long memberId, String tokenType, long expireMs) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claim("memberId", memberId) // JWT Payload에 사용자 ID 저장
                .claim("type", tokenType) // 토큰 타입 저장
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireMs))
                .signWith(secretKey) // JWT에 Secret Key를 사용해서 서명
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validateTokenType(token, "access");
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, "refresh");
    }

    // 검증
    private boolean validateTokenType(String token, String expectedType) {
        try {
            /**
             * JWT 형식 검증
             * Signature 검증
             * Expiration 검증
             */
            Claims claims = parseClaims(token);

            return expectedType.equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // JWT에서 memberId Claim 추출
    public Long getMemberId(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }

    public LocalDateTime getRefreshExpiresAt() {
        return LocalDateTime.now().plusSeconds(refreshExpireMs / 1000);
    }

    public long getRefreshMaxAgeSeconds() {
        return refreshExpireMs / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // JWT Signature 검증
                .build()
                .parseSignedClaims(token) // 서명된 JWT 파싱
                .getPayload();
    }
}
