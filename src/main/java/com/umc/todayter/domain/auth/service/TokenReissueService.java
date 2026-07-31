package com.umc.todayter.domain.auth.service;

import com.umc.todayter.domain.auth.exception.AuthErrorCode;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.jwt.JwtProvider;
import com.umc.todayter.global.security.jwt.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenReissueService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;

    @Transactional
    public AuthTokenResult reissue(String refreshToken) {
        validateRefreshTokenExists(refreshToken);

        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long memberId = jwtProvider.getMemberId(refreshToken);

        Member member = memberRepository
                .findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateStoredRefreshToken(member, refreshToken);

        return authTokenService.issueTokens(member);
    }

    private void validateRefreshTokenExists(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_MISSING);
        }
    }

    private void validateStoredRefreshToken(Member member, String refreshToken) {
        String storedHash = member.getRefreshTokenHash();
        LocalDateTime storedExpiresAt = member.getRefreshTokenExpiresAt();

        if (storedHash == null || storedExpiresAt == null || storedExpiresAt.isBefore(LocalDateTime.now())) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        String requestTokenHash = refreshTokenHasher.hash(refreshToken);

        if (!storedHash.equals(requestTokenHash)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }
    }
}
