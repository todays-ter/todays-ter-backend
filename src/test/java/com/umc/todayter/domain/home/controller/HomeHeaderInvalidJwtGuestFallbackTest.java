package com.umc.todayter.domain.home.controller;

import com.umc.todayter.domain.home.service.EnergyRoutineService;
import com.umc.todayter.domain.home.service.HomeService;
import com.umc.todayter.domain.home.service.TodayEnergyService;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import com.umc.todayter.global.config.SecurityConfig;
import com.umc.todayter.global.security.context.CurrentUserContextResolver;
import com.umc.todayter.global.security.jwt.JwtProvider;
import com.umc.todayter.global.util.GuestCookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HomeController.class)
@Import({
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        SecurityConfig.class,
        CurrentUserContextResolver.class,
        HomeService.class,
        HomeHeaderInvalidJwtGuestFallbackTest.FixedClockConfig.class
})
class HomeHeaderInvalidJwtGuestFallbackTest {

    private static final String INVALID_TOKEN = "invalid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestSessionRepository guestSessionRepository;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private TodayEnergyService todayEnergyService;

    @MockitoBean
    private EnergyRoutineService energyRoutineService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @AfterEach
    void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void invalidJwtWithValidGuestCookieFallsBackToGuest() throws Exception {
        GuestSession guestSession = GuestSession.create(
                "guest-id",
                LocalDateTime.of(2026, 7, 31, 10, 1)
        );
        ReflectionTestUtils.setField(guestSession, "id", 10L);

        when(jwtProvider.validateAccessToken(INVALID_TOKEN)).thenReturn(false);
        when(guestSessionRepository.findByGuestId("guest-id")).thenReturn(Optional.of(guestSession));

        mockMvc.perform(get("/home/header")
                        .header("Authorization", "Bearer " + INVALID_TOKEN)
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userType").value("GUEST"))
                .andExpect(jsonPath("$.result.nickname").doesNotExist())
                .andExpect(jsonPath("$.result.greeting").value("안녕하세요!"));
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(
                    Instant.parse("2026-07-31T01:00:00Z"),
                    ZoneId.of("Asia/Seoul")
            );
        }
    }
}
