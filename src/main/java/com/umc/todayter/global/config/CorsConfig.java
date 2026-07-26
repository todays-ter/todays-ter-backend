package com.umc.todayter.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * 프론트엔드 애플리케이션에서 백엔드 API를 호출할 수 있도록
     * 전역 CORS 정책을 설정함
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        /**
         * 요청을 허용할 프론트엔드 Origin 목록
         *
         * 3000: React
         * 5173: Vite
         *
         * 인증 쿠키를 허용하므로 "*" 대신 Origin을 명시해야 함
         */
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        // 프론트엔드에서 사용할 HTTP 메서드 허용
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        /**
         * 브라우저가 실제 요청과 Preflight 요청에서
         * 전송할 수 있는 요청 헤더 지정
         */
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        /**
         * Refresh Token과 같은 쿠키를 포함한 요청을 허용
         * 프론트에서도 withCredentials 또는 credentials: "include" 설정이 필요하다고 함
         */
        config.setAllowCredentials(true);

        // 모든 API 경로에 위 CORS 정책을 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
