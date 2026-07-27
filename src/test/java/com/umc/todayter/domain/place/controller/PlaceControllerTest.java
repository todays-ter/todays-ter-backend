package com.umc.todayter.domain.place.controller;

import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListResponse;
import com.umc.todayter.domain.place.dto.response.RegionFilterResponse;
import com.umc.todayter.domain.place.dto.response.ThemeFilterResponse;
import com.umc.todayter.domain.place.exception.PlaceErrorCode;
import com.umc.todayter.domain.place.service.PlaceService;
import com.umc.todayter.domain.place.service.PlaceThumbnailService;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.config.SecurityConfig;
import com.umc.todayter.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = PlaceController.class)
@Import({
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        SecurityConfig.class
})
class PlaceControllerTest {

    private static final String VALID_TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private PlaceThumbnailService placeThumbnailService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getExploreFilters_returnsApiResponseWithoutAccessToken() throws Exception {
        when(placeService.getExploreFilters()).thenReturn(new ExploreFiltersResponse(
                List.of(new RegionFilterResponse("ALL", "all", 0)),
                List.of(new ThemeFilterResponse("LOVE", "love", 0L, 1)),
                List.of(new ElementFilterResponse("ALL", "all", 0))
        ));

        mockMvc.perform(get("/places/explore-filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.regions").isArray())
                .andExpect(jsonPath("$.result.themes").isArray())
                .andExpect(jsonPath("$.result.elements").isArray());
    }

    @Test
    void getMyPlaces_withoutAuthorizationHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/places/me").param("type", "saved"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void getMyPlaces_savedType_returnsPlaceList() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeService.getMyPlaces("saved")).thenReturn(new PlaceListResponse(List.of(
                new PlaceListItemResponse(1L, "경복궁", "/places/1/thumbnail", List.of("관계", "일·커리어"), LocalDate.of(2026, 6, 29), "화")
        )));

        mockMvc.perform(get("/places/me")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .param("type", "saved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.places[0].placeId").value(1))
                .andExpect(jsonPath("$.result.places[0].placeName").value("경복궁"))
                .andExpect(jsonPath("$.result.places[0].categories[0]").value("관계"))
                .andExpect(jsonPath("$.result.places[0].element").value("화"));
    }

    @Test
    void getMyPlaces_visitedType_returnsPlaceList() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeService.getMyPlaces("visited")).thenReturn(new PlaceListResponse(List.of()));

        mockMvc.perform(get("/places/me")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .param("type", "visited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.places").isArray());
    }

    @Test
    void getMyPlaces_missingType_returnsBadRequest() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeService.getMyPlaces(null)).thenThrow(new CustomException(PlaceErrorCode.MISSING_TYPE_PARAMETER));

        mockMvc.perform(get("/places/me")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLACE400_1"));
    }

    @Test
    void getMyPlaces_invalidType_returnsBadRequest() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeService.getMyPlaces("bookmarked")).thenThrow(new CustomException(PlaceErrorCode.INVALID_TYPE_PARAMETER));

        mockMvc.perform(get("/places/me")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .param("type", "bookmarked"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLACE400_2"));
    }

    @Test
    void getThumbnail_returnsFoundWithoutBodyAndNoStore() throws Exception {
        URI photoUri = URI.create("https://lh3.googleusercontent.com/photo");
        when(placeThumbnailService.getThumbnailUri(1L)).thenReturn(photoUri);

        mockMvc.perform(get("/places/1/thumbnail"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, photoUri.toString()))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(content().string(""));
    }

    @Test
    void getThumbnail_allowsRequestWithoutAuthorizationHeader() throws Exception {
        URI photoUri = URI.create("https://lh3.googleusercontent.com/photo");
        when(placeThumbnailService.getThumbnailUri(1L)).thenReturn(photoUri);

        mockMvc.perform(get("/places/1/thumbnail"))
                .andExpect(status().isFound());
    }

    @Test
    void getThumbnail_returnsApiResponseWhenPlaceNotFound() throws Exception {
        when(placeThumbnailService.getThumbnailUri(1L))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND));

        mockMvc.perform(get("/places/1/thumbnail"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON404"));
    }

    @Test
    void getThumbnail_returnsApiResponseWhenGooglePlacesFails() throws Exception {
        when(placeThumbnailService.getThumbnailUri(1L))
                .thenThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/places/1/thumbnail"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON500"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("external error body"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("test-google-api-key"))));
    }
}
