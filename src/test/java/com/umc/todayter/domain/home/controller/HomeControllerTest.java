package com.umc.todayter.domain.home.controller;

import com.umc.todayter.domain.home.dto.request.HomeRecommendedPlaceQuery;
import com.umc.todayter.domain.home.dto.response.HomeHeaderResponse;
import com.umc.todayter.domain.home.dto.response.EnergyRoutineElementResponse;
import com.umc.todayter.domain.home.dto.response.EnergyRoutineItemResponse;
import com.umc.todayter.domain.home.dto.response.EnergyRoutinesResponse;
import com.umc.todayter.domain.home.dto.response.HomeLoginPromptResponse;
import com.umc.todayter.domain.home.dto.response.HomeRecommendedPlaceItemResponse;
import com.umc.todayter.domain.home.dto.response.HomeRecommendedPlacesResponse;
import com.umc.todayter.domain.home.dto.response.TodayEnergyElementResponse;
import com.umc.todayter.domain.home.dto.response.TodayEnergyResponse;
import com.umc.todayter.domain.home.exception.HomeErrorCode;
import com.umc.todayter.domain.home.service.EnergyRoutineService;
import com.umc.todayter.domain.home.service.HomeService;
import com.umc.todayter.domain.home.service.RecommendedPlaceService;
import com.umc.todayter.domain.home.service.TodayEnergyService;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.exception.code.FortuneReportErrorCode;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.controller.PlaceController;
import com.umc.todayter.domain.place.dto.response.EditorPickResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchAppliedFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchPageResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchResponse;
import com.umc.todayter.domain.place.service.EditorPickService;
import com.umc.todayter.domain.place.service.PlaceSearchService;
import com.umc.todayter.domain.place.service.PlaceService;
import com.umc.todayter.domain.place.service.PlaceThumbnailService;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.config.SecurityConfig;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserContextResolver;
import com.umc.todayter.global.security.context.CurrentUserType;
import com.umc.todayter.global.security.jwt.JwtProvider;
import com.umc.todayter.global.util.GuestCookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        HomeController.class,
        PlaceController.class
})
@Import({
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        SecurityConfig.class
})
class HomeControllerTest {

    private static final String VALID_TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrentUserContextResolver currentUserContextResolver;

    @MockitoBean
    private HomeService homeService;

    @MockitoBean
    private TodayEnergyService todayEnergyService;

    @MockitoBean
    private EnergyRoutineService energyRoutineService;

    @MockitoBean
    private RecommendedPlaceService recommendedPlaceService;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private PlaceSearchService placeSearchService;

    @MockitoBean
    private EditorPickService editorPickService;

    @MockitoBean
    private PlaceThumbnailService placeThumbnailService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @AfterEach
    void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void getHeaderWithMemberContextReturnsOk() throws Exception {
        CurrentUserContext context = CurrentUserContext.forMember(1L);
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(currentUserContextResolver.resolve(null)).thenReturn(context);
        when(homeService.getHeader(context)).thenReturn(new HomeHeaderResponse(
                CurrentUserType.MEMBER,
                LocalDate.of(2026, 7, 31),
                DayOfWeek.FRIDAY,
                "현우",
                "안녕하세요 현우님!",
                "오늘도 좋은 기운 충전해요"
        ));

        mockMvc.perform(get("/home/header")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.userType").value("MEMBER"))
                .andExpect(jsonPath("$.result.date").value("2026-07-31"))
                .andExpect(jsonPath("$.result.dayOfWeek").value("FRIDAY"))
                .andExpect(jsonPath("$.result.nickname").value("현우"))
                .andExpect(jsonPath("$.result.greeting").value("안녕하세요 현우님!"))
                .andExpect(jsonPath("$.result.subGreeting").value("오늘도 좋은 기운 충전해요"));

        verify(currentUserContextResolver).resolve(null);
        verify(homeService).getHeader(context);
    }

    @Test
    void getHeaderWithGuestContextReturnsOkAndPassesCookie() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(homeService.getHeader(context)).thenReturn(new HomeHeaderResponse(
                CurrentUserType.GUEST,
                LocalDate.of(2026, 7, 31),
                DayOfWeek.FRIDAY,
                null,
                "안녕하세요!",
                "오늘도 좋은 기운 충전해요"
        ));

        mockMvc.perform(get("/home/header")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.userType").value("GUEST"))
                .andExpect(jsonPath("$.result.date").value("2026-07-31"))
                .andExpect(jsonPath("$.result.dayOfWeek").value("FRIDAY"))
                .andExpect(jsonPath("$.result.nickname").doesNotExist())
                .andExpect(jsonPath("$.result.greeting").value("안녕하세요!"))
                .andExpect(jsonPath("$.result.subGreeting").value("오늘도 좋은 기운 충전해요"));

        verify(currentUserContextResolver).resolve("guest-id");
        verify(homeService).getHeader(context);
    }

    @Test
    void getHeaderWorksWithoutBodyOrQueryParameter() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(homeService.getHeader(context)).thenReturn(new HomeHeaderResponse(
                CurrentUserType.GUEST,
                LocalDate.of(2026, 7, 31),
                DayOfWeek.FRIDAY,
                null,
                "안녕하세요!",
                "오늘도 좋은 기운 충전해요"
        ));

        mockMvc.perform(get("/home/header")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isOk());
    }

    @Test
    void getTodayEnergyWithMemberContextReturnsOk() throws Exception {
        CurrentUserContext context = CurrentUserContext.forMember(1L);
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(currentUserContextResolver.resolve(null)).thenReturn(context);
        when(todayEnergyService.getTodayEnergy(context)).thenReturn(new TodayEnergyResponse(
                LocalDate.of(2026, 8, 3),
                new TodayEnergyElementResponse(FiveElement.WATER, FiveElement.WATER.getLabel()),
                "summary"
        ));

        mockMvc.perform(get("/home/today-energy")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.date").value("2026-08-03"))
                .andExpect(jsonPath("$.result.element.code").value("WATER"))
                .andExpect(jsonPath("$.result.element.name").value(FiveElement.WATER.getLabel()))
                .andExpect(jsonPath("$.result.description").value("summary"))
                .andExpect(jsonPath("$.result.reportId").doesNotExist())
                .andExpect(jsonPath("$.result.memberId").doesNotExist())
                .andExpect(jsonPath("$.result.guestSessionId").doesNotExist())
                .andExpect(jsonPath("$.result.guestId").doesNotExist())
                .andExpect(jsonPath("$.result.onboardingId").doesNotExist())
                .andExpect(jsonPath("$.result.status").doesNotExist())
                .andExpect(jsonPath("$.result.currentStep").doesNotExist())
                .andExpect(jsonPath("$.result.progress").doesNotExist())
                .andExpect(jsonPath("$.result.reportContent").doesNotExist())
                .andExpect(jsonPath("$.result.retryCount").doesNotExist())
                .andExpect(jsonPath("$.result.completedAt").doesNotExist());

        verify(currentUserContextResolver).resolve(null);
        verify(todayEnergyService).getTodayEnergy(context);
    }

    @Test
    void getTodayEnergyWithGuestContextReturnsOkAndPassesCookie() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(todayEnergyService.getTodayEnergy(context)).thenReturn(new TodayEnergyResponse(
                LocalDate.of(2026, 8, 3),
                new TodayEnergyElementResponse(FiveElement.FIRE, FiveElement.FIRE.getLabel()),
                "guest summary"
        ));

        mockMvc.perform(get("/home/today-energy")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.date").value("2026-08-03"))
                .andExpect(jsonPath("$.result.element.code").value("FIRE"))
                .andExpect(jsonPath("$.result.element.name").value(FiveElement.FIRE.getLabel()))
                .andExpect(jsonPath("$.result.description").value("guest summary"));

        verify(currentUserContextResolver).resolve("guest-id");
        verify(todayEnergyService).getTodayEnergy(context);
    }

    @Test
    void getEnergyRoutinesWithMemberContextReturnsOk() throws Exception {
        CurrentUserContext context = CurrentUserContext.forMember(1L);
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(currentUserContextResolver.resolve(null)).thenReturn(context);
        when(energyRoutineService.getEnergyRoutines(context)).thenReturn(new EnergyRoutinesResponse(
                EnergyRoutineElementResponse.from(FiveElement.WATER),
                List.of(
                        new EnergyRoutineItemResponse(1, "물가", "강이나 하천을 따라 천천히 걸어봐요"),
                        new EnergyRoutineItemResponse(2, "고요", "도서관에서 시간을 보내봐요"),
                        new EnergyRoutineItemResponse(3, "사유", "떠오르는 감정을 짧게 적어봐요")
                )
        ));

        mockMvc.perform(get("/home/energy-routines")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.element.code").value("WATER"))
                .andExpect(jsonPath("$.result.element.name").value(FiveElement.WATER.getLabel()))
                .andExpect(jsonPath("$.result.routines[0].order").value(1))
                .andExpect(jsonPath("$.result.routines[0].type").value("물가"))
                .andExpect(jsonPath("$.result.routines[0].text").value("강이나 하천을 따라 천천히 걸어봐요"))
                .andExpect(jsonPath("$.result.routines[1].order").value(2))
                .andExpect(jsonPath("$.result.routines[1].type").value("고요"))
                .andExpect(jsonPath("$.result.routines[1].text").value("도서관에서 시간을 보내봐요"))
                .andExpect(jsonPath("$.result.routines[2].order").value(3))
                .andExpect(jsonPath("$.result.routines[2].type").value("사유"))
                .andExpect(jsonPath("$.result.routines[2].text").value("떠오르는 감정을 짧게 적어봐요"))
                .andExpect(jsonPath("$.result.date").doesNotExist())
                .andExpect(jsonPath("$.result.reportId").doesNotExist())
                .andExpect(jsonPath("$.result.memberId").doesNotExist())
                .andExpect(jsonPath("$.result.guestSessionId").doesNotExist())
                .andExpect(jsonPath("$.result.guestId").doesNotExist())
                .andExpect(jsonPath("$.result.onboardingId").doesNotExist())
                .andExpect(jsonPath("$.result.status").doesNotExist())
                .andExpect(jsonPath("$.result.currentStep").doesNotExist())
                .andExpect(jsonPath("$.result.progress").doesNotExist())
                .andExpect(jsonPath("$.result.reportContent").doesNotExist())
                .andExpect(jsonPath("$.result.retryCount").doesNotExist())
                .andExpect(jsonPath("$.result.completedAt").doesNotExist())
                .andExpect(jsonPath("$.result.primaryElements").doesNotExist())
                .andExpect(jsonPath("$.result.complementElement").doesNotExist());

        verify(currentUserContextResolver).resolve(null);
        verify(energyRoutineService).getEnergyRoutines(context);
    }

    @Test
    void getEnergyRoutinesWithGuestContextReturnsOkAndPassesCookie() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(energyRoutineService.getEnergyRoutines(context)).thenReturn(new EnergyRoutinesResponse(
                EnergyRoutineElementResponse.from(FiveElement.FIRE),
                List.of(new EnergyRoutineItemResponse(1, "햇빛", "text"))
        ));

        mockMvc.perform(get("/home/energy-routines")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.element.code").value("FIRE"))
                .andExpect(jsonPath("$.result.element.name").value(FiveElement.FIRE.getLabel()))
                .andExpect(jsonPath("$.result.routines[0].order").value(1))
                .andExpect(jsonPath("$.result.routines[0].type").value("햇빛"))
                .andExpect(jsonPath("$.result.routines[0].text").value("text"));

        verify(currentUserContextResolver).resolve("guest-id");
        verify(energyRoutineService).getEnergyRoutines(context);
    }

    @Test
    void resolverUnauthorizedReturnsUnauthorized() throws Exception {
        when(currentUserContextResolver.resolve(null)).thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/home/header"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(currentUserContextResolver).resolve(null);
    }

    @Test
    void todayEnergyResolverUnauthorizedReturnsUnauthorized() throws Exception {
        when(currentUserContextResolver.resolve(null)).thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/home/today-energy"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(currentUserContextResolver).resolve(null);
    }

    @Test
    void energyRoutinesResolverUnauthorizedReturnsUnauthorized() throws Exception {
        when(currentUserContextResolver.resolve(null)).thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/home/energy-routines"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(currentUserContextResolver).resolve(null);
    }

    @Test
    void getRecommendedPlaceWithMemberContextReturnsOk() throws Exception {
        CurrentUserContext context = CurrentUserContext.forMember(1L);
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(currentUserContextResolver.resolve(null)).thenReturn(context);
        when(recommendedPlaceService.getRecommendedPlaces(eq(context), any(HomeRecommendedPlaceQuery.class), anyString()))
                .thenReturn(new HomeRecommendedPlacesResponse(
                        CurrentUserType.MEMBER,
                        false,
                        1,
                        3,
                        List.of(new HomeRecommendedPlaceItemResponse(
                                25L,
                                1,
                                "창경궁",
                                "http://localhost/places/25/thumbnail",
                                ElementType.EARTH,
                                95,
                                "reason",
                                3.5,
                                4.7
                        )),
                        null
                ));

        mockMvc.perform(get("/home/recommended-place")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .param("latitude", "37.5665")
                        .param("longitude", "126.9780"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.userType").value("MEMBER"))
                .andExpect(jsonPath("$.result.isLimited").value(false))
                .andExpect(jsonPath("$.result.visibleCount").value(1))
                .andExpect(jsonPath("$.result.totalCount").value(3))
                .andExpect(jsonPath("$.result.recommendations[0].placeId").value(25))
                .andExpect(jsonPath("$.result.recommendations[0].rankOrder").value(1))
                .andExpect(jsonPath("$.result.recommendations[0].placeName").value("창경궁"))
                .andExpect(jsonPath("$.result.recommendations[0].thumbnailUrl").value("http://localhost/places/25/thumbnail"))
                .andExpect(jsonPath("$.result.recommendations[0].placeElement").value("EARTH"))
                .andExpect(jsonPath("$.result.recommendations[0].matchPercentage").value(95))
                .andExpect(jsonPath("$.result.recommendations[0].recommendationReason").value("reason"))
                .andExpect(jsonPath("$.result.recommendations[0].distanceKm").value(3.5))
                .andExpect(jsonPath("$.result.recommendations[0].averageRating").value(4.7))
                .andExpect(jsonPath("$.result.loginPrompt").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].recommendationId").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].reportId").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].memberId").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].guestSessionId").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].guestId").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].onboardingId").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].isGuestLocked").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].actionSuggestion").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].shareToken").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].snapshotId").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].reportContent").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].primaryElements").doesNotExist())
                .andExpect(jsonPath("$.result.recommendations[0].complementElement").doesNotExist());
    }

    @Test
    void getRecommendedPlaceWithGuestContextReturnsPrompt() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(recommendedPlaceService.getRecommendedPlaces(eq(context), any(HomeRecommendedPlaceQuery.class), anyString()))
                .thenReturn(new HomeRecommendedPlacesResponse(
                        CurrentUserType.GUEST,
                        true,
                        0,
                        0,
                        List.of(),
                        HomeLoginPromptResponse.guest()
                ));

        mockMvc.perform(get("/home/recommended-place")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userType").value("GUEST"))
                .andExpect(jsonPath("$.result.isLimited").value(true))
                .andExpect(jsonPath("$.result.visibleCount").value(0))
                .andExpect(jsonPath("$.result.totalCount").value(0))
                .andExpect(jsonPath("$.result.recommendations").isEmpty())
                .andExpect(jsonPath("$.result.loginPrompt.title").value("로그인 후 더 많은 터를 탐색해보세요"))
                .andExpect(jsonPath("$.result.loginPrompt.buttonText").value("로그인/회원가입 하러가기"));

        verify(currentUserContextResolver).resolve("guest-id");
    }

    @Test
    void recommendedPlaceInvalidCoordinateReturnsHome400_1BeforeResolvingUser() throws Exception {
        mockMvc.perform(get("/home/recommended-place")
                        .param("latitude", "37.5665"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("HOME400_1"));
    }

    @Test
    void recommendedPlaceErrorsUseHomeCodes() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(recommendedPlaceService.getRecommendedPlaces(eq(context), any(HomeRecommendedPlaceQuery.class), anyString()))
                .thenThrow(new CustomException(HomeErrorCode.FORTUNE_REPORT_PROCESSING));

        mockMvc.perform(get("/home/recommended-place")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HOME409_1"));
    }

    @Test
    void todayEnergyReportNotFoundReturnsNotFound() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(todayEnergyService.getTodayEnergy(context))
                .thenThrow(new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));

        mockMvc.perform(get("/home/today-energy")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FORTUNE404_1"));
    }

    @Test
    void todayEnergyReportContentUnavailableReturnsInternalServerError() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(todayEnergyService.getTodayEnergy(context))
                .thenThrow(new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE));

        mockMvc.perform(get("/home/today-energy")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("FORTUNE500_1"));
    }

    @Test
    void energyRoutinesReportNotFoundReturnsNotFound() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(energyRoutineService.getEnergyRoutines(context))
                .thenThrow(new CustomException(FortuneReportErrorCode.REPORT_NOT_FOUND));

        mockMvc.perform(get("/home/energy-routines")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FORTUNE404_1"));
    }

    @Test
    void energyRoutinesReportContentUnavailableReturnsInternalServerError() throws Exception {
        CurrentUserContext context = CurrentUserContext.forGuest(10L, "guest-id");
        when(currentUserContextResolver.resolve("guest-id")).thenReturn(context);
        when(energyRoutineService.getEnergyRoutines(context))
                .thenThrow(new CustomException(FortuneReportErrorCode.REPORT_CONTENT_UNAVAILABLE));

        mockMvc.perform(get("/home/energy-routines")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "guest-id")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("FORTUNE500_1"));
    }

    @Test
    void invalidGuestIdReturnsUnauthorized() throws Exception {
        when(currentUserContextResolver.resolve("wrong-guest-id"))
                .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/home/header")
                        .cookie(new Cookie(GuestCookieUtil.COOKIE_NAME, "wrong-guest-id")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void unauthenticatedGetHeaderReachesControllerAndResolverReturnsUnauthorized() throws Exception {
        when(currentUserContextResolver.resolve(null)).thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/home/header"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(currentUserContextResolver).resolve(null);
    }

    @Test
    void recommendedPlaceIsPublicButRequiresUserContext() throws Exception {
        when(currentUserContextResolver.resolve(null)).thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/home/recommended-place"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void existingPublicPlaceApisRemainPublic() throws Exception {
        when(placeSearchService.searchPlaces(any(), anyString())).thenReturn(new PlaceSearchResponse(
                new PlaceSearchAppliedFiltersResponse(null, null, null, null),
                List.of(),
                new PlaceSearchPageResponse(0, 20, 0, 0, false)
        ));
        when(editorPickService.getEditorPicks(eq(3), anyString())).thenReturn(new EditorPickResponse(List.of()));
        when(placeService.getExploreFilters()).thenReturn(new ExploreFiltersResponse(List.of(), List.of(), List.of()));
        when(placeThumbnailService.getThumbnailUri(1L)).thenReturn(URI.create("https://example.com/photo.jpg"));

        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"));

        mockMvc.perform(get("/places/editor-picks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"));

        mockMvc.perform(get("/places/explore-filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"));

        mockMvc.perform(get("/places/1/thumbnail"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/photo.jpg"));
    }

    @Test
    void getMyPlacesStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/places/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }
}
