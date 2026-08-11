package com.umc.todayter.global.security.jwt;

import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.security.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Authorization 헤더가 없으면 게스트 요청일 수 있으므로 그대로 진행
        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);

            return;
        }

        String accessToken = resolveAccessToken(authorizationHeader);

        // 헤더가 존재하지만 Bearer 형식이 아니거나 토큰이 비어 있는 경우
        if (accessToken == null) {
            writeUnauthorizedResponse(response);

            return;
        }

        // 만료, 변조 또는 Access Token이 아닌 경우
        if (!jwtProvider.validateAccessToken(accessToken)) {
            writeUnauthorizedResponse(response);

            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            Long memberId = jwtProvider.getMemberId(accessToken);

            AuthPrincipal principal = new AuthPrincipal(memberId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveAccessToken(String authorizationHeader) {
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        return token.isBlank() ? null : token;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(ErrorCode.UNAUTHORIZED
                .getHttpStatus()
                .value()
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(), ApiResponse.onFailure(null, ErrorCode.UNAUTHORIZED)
        );
    }
}
