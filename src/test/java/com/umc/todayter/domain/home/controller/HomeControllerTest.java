package com.umc.todayter.domain.home.controller;

import com.umc.todayter.domain.home.dto.response.HomeHeaderResponse;
import com.umc.todayter.domain.home.service.HomeService;
import com.umc.todayter.domain.place.controller.PlaceController;
import com.umc.todayter.domain.place.dto.response.EditorPickResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchAppliedFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchPageResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchResponse;
import com.umc.todayter.domain.place.service.EditorPickService;
import com.umc.todayter.domain.place.service.PlaceDetailService;
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
    private PlaceService placeService;

    @MockitoBean
    private PlaceSearchService placeSearchService;

    @MockitoBean
    private EditorPickService editorPickService;

    @MockitoBean
    private PlaceThumbnailService placeThumbnailService;

    @MockitoBean
    private PlaceDetailService placeDetailService;

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
    void resolverUnauthorizedReturnsUnauthorized() throws Exception {
        when(currentUserContextResolver.resolve(null)).thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/home/header"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(currentUserContextResolver).resolve(null);
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
    void unimplementedHomePathsAreNotPublic() throws Exception {
        mockMvc.perform(get("/home/today-energy"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));

        mockMvc.perform(get("/home/energy-routines"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));

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
