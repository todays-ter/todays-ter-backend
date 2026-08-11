package com.umc.todayter.domain.place.controller;

import com.umc.todayter.domain.place.dto.response.EditorPickItemResponse;
import com.umc.todayter.domain.place.dto.response.EditorPickResponse;
import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceBookmarkResponse;
import com.umc.todayter.domain.place.dto.response.PlaceDetailResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListResponse;
import com.umc.todayter.domain.place.dto.response.PlaceReviewItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceReviewListResponse;
import com.umc.todayter.domain.record.dto.response.ImageInfo;
import com.umc.todayter.domain.place.dto.response.PlaceSearchAppliedFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchPageResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchTypeResponse;
import com.umc.todayter.domain.place.dto.request.PlaceSearchRequest;
import com.umc.todayter.domain.place.service.EditorPickService;
import com.umc.todayter.domain.place.service.PlaceDetailService;
import com.umc.todayter.domain.place.service.PlaceSearchService;
import com.umc.todayter.domain.place.service.PlaceShareCardService;
import com.umc.todayter.domain.place.dto.response.PlaceShareCardResponse;
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
import org.mockito.ArgumentCaptor;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private PlaceSearchService placeSearchService;

    @MockitoBean
    private EditorPickService editorPickService;

    @MockitoBean
    private PlaceThumbnailService placeThumbnailService;

    @MockitoBean
    private PlaceDetailService placeDetailService;

    @MockitoBean
    private PlaceShareCardService placeShareCardService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void searchPlaces_returnsApiResponseWithoutAccessToken() throws Exception {
        when(placeSearchService.searchPlaces(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("http://localhost")))
                .thenReturn(new PlaceSearchResponse(
                        new PlaceSearchAppliedFiltersResponse("palace", "SEOUL", "WEALTH", "EARTH"),
                        List.of(new PlaceSearchItemResponse(
                                1L,
                                "Gyeongbokgung",
                                "http://localhost/places/1/thumbnail",
                                "summary",
                                new PlaceSearchTypeResponse("EARTH", "earth"),
                                new PlaceSearchTypeResponse("WEALTH", "wealth"),
                                4.7,
                                3.5
                        )),
                        new PlaceSearchPageResponse(0, 20, 1, 1, false)
                ));

        mockMvc.perform(get("/places")
                        .param("keyword", " palace ")
                        .param("regionCode", "SEOUL")
                        .param("themeType", "WEALTH")
                        .param("elementType", "EARTH")
                        .param("latitude", "37.5665")
                        .param("longitude", "126.9780"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.appliedFilters.keyword").value("palace"))
                .andExpect(jsonPath("$.result.content[0].placeId").value(1))
                .andExpect(jsonPath("$.result.content[0].thumbnailUrl").value("http://localhost/places/1/thumbnail"))
                .andExpect(jsonPath("$.result.content[0].element.code").value("EARTH"))
                .andExpect(jsonPath("$.result.content[0].theme.code").value("WEALTH"))
                .andExpect(jsonPath("$.result.content[0].distanceKm").value(3.5))
                .andExpect(jsonPath("$.result.page.number").value(0))
                .andExpect(jsonPath("$.result.page.size").value(20));
    }

    @Test
    void searchPlaces_usesDefaultPageAndSize() throws Exception {
        when(placeSearchService.searchPlaces(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new PlaceSearchResponse(
                        new PlaceSearchAppliedFiltersResponse(null, null, null, null),
                        List.of(),
                        new PlaceSearchPageResponse(0, 20, 0, 0, false)
                ));

        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.page.number").value(0))
                .andExpect(jsonPath("$.result.page.size").value(20));

        ArgumentCaptor<PlaceSearchRequest> requestCaptor = ArgumentCaptor.forClass(PlaceSearchRequest.class);
        verify(placeSearchService).searchPlaces(requestCaptor.capture(), org.mockito.ArgumentMatchers.anyString());
        PlaceSearchRequest request = requestCaptor.getValue();
        assertThat(request.getPage()).isZero();
        assertThat(request.getSize()).isEqualTo(20);
        assertThat(request.getKeyword()).isNull();
        assertThat(request.getLatitude()).isNull();
        assertThat(request.getLongitude()).isNull();
    }

    @Test
    void searchPlaces_returnsOkWithEmptyContent() throws Exception {
        when(placeSearchService.searchPlaces(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new PlaceSearchResponse(
                        new PlaceSearchAppliedFiltersResponse(null, null, null, null),
                        List.of(),
                        new PlaceSearchPageResponse(0, 20, 0, 0, false)
                ));

        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isEmpty());
    }

    @Test
    void searchPlaces_blankKeywordReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places").param("keyword", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    void searchPlaces_invalidPageReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    void searchPlaces_invalidSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));

        mockMvc.perform(get("/places").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    void searchPlaces_singleCoordinateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places").param("latitude", "37.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));

        mockMvc.perform(get("/places").param("longitude", "127.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    void searchPlaces_outOfRangeCoordinateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places")
                        .param("latitude", "91")
                        .param("longitude", "127.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));

        mockMvc.perform(get("/places")
                        .param("latitude", "37.5")
                        .param("longitude", "181"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    void searchPlaces_invalidEnumReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places").param("regionCode", "ALL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    void searchPlaces_invalidNumberTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places").param("latitude", "not-number").param("longitude", "127.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

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
    void getEditorPicks_withoutLimitUsesDefaultLimitAndReturnsApiResponseWithoutAccessToken() throws Exception {
        when(editorPickService.getEditorPicks(org.mockito.ArgumentMatchers.eq(3), org.mockito.ArgumentMatchers.eq("http://localhost")))
                .thenReturn(new EditorPickResponse(List.of(new EditorPickItemResponse(
                        1L,
                        "Gyeongbokgung",
                        "http://localhost/places/1/thumbnail",
                        "summary",
                        "description",
                        new PlaceSearchTypeResponse("EARTH", "earth"),
                        new PlaceSearchTypeResponse("WEALTH", "wealth"),
                        4.7
                ))));

        mockMvc.perform(get("/places/editor-picks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.content[0].placeId").value(1))
                .andExpect(jsonPath("$.result.content[0].placeName").value("Gyeongbokgung"))
                .andExpect(jsonPath("$.result.content[0].thumbnailUrl").value("http://localhost/places/1/thumbnail"))
                .andExpect(jsonPath("$.result.content[0].summary").value("summary"))
                .andExpect(jsonPath("$.result.content[0].description").value("description"))
                .andExpect(jsonPath("$.result.content[0].element.code").value("EARTH"))
                .andExpect(jsonPath("$.result.content[0].element.name").value("earth"))
                .andExpect(jsonPath("$.result.content[0].theme.code").value("WEALTH"))
                .andExpect(jsonPath("$.result.content[0].theme.name").value("wealth"))
                .andExpect(jsonPath("$.result.content[0].averageRating").value(4.7));

        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(editorPickService).getEditorPicks(limitCaptor.capture(), org.mockito.ArgumentMatchers.eq("http://localhost"));
        assertThat(limitCaptor.getValue()).isEqualTo(3);
    }

    @Test
    void getEditorPicks_withLimitOnePassesLimitToService() throws Exception {
        when(editorPickService.getEditorPicks(org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new EditorPickResponse(List.of()));

        mockMvc.perform(get("/places/editor-picks").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isEmpty());

        verify(editorPickService).getEditorPicks(org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getEditorPicks_withLimitTenPassesLimitToService() throws Exception {
        when(editorPickService.getEditorPicks(org.mockito.ArgumentMatchers.eq(10), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new EditorPickResponse(List.of()));

        mockMvc.perform(get("/places/editor-picks").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isEmpty());

        verify(editorPickService).getEditorPicks(org.mockito.ArgumentMatchers.eq(10), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getEditorPicks_returnsOkWithEmptyContent() throws Exception {
        when(editorPickService.getEditorPicks(org.mockito.ArgumentMatchers.eq(3), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new EditorPickResponse(List.of()));

        mockMvc.perform(get("/places/editor-picks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isEmpty());
    }

    @Test
    void getEditorPicks_invalidLimitReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/places/editor-picks").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));

        mockMvc.perform(get("/places/editor-picks").param("limit", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));

        mockMvc.perform(get("/places/editor-picks").param("limit", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verify(editorPickService, never()).getEditorPicks(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
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
    void getMyPlaces_stillResolvesToPlaceListEndpoint_notShadowedByPlaceDetail() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeService.getMyPlaces("saved")).thenReturn(new PlaceListResponse(List.of()));

        mockMvc.perform(get("/places/me")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .param("type", "saved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.places").isArray());

        verify(placeDetailService, never()).getPlaceDetail(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getPlaceDetail_returnsApiResponseWithValidToken() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeDetailService.getPlaceDetail(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("http://localhost")))
                .thenReturn(new PlaceDetailResponse(
                        1L,
                        "청계천 모전교",
                        "http://localhost/places/1/thumbnail",
                        "수",
                        List.of("감정 회복"),
                        new PlaceDetailResponse.PlaceDescription(
                                "이 터의 특징은 무엇인가요?",
                                "수(水) 기운이 강해 감정 정리와 회복에 좋고 오늘의 흐름과 잘 맞아요."
                        ),
                        "서울 중구 무교동",
                        37.5665,
                        126.9780,
                        "https://map.example.com/places/1",
                        9L,
                        false,
                        false
                ));

        mockMvc.perform(get("/places/1")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.placeId").value(1))
                .andExpect(jsonPath("$.result.placeName").value("청계천 모전교"))
                .andExpect(jsonPath("$.result.imageUrl").value("http://localhost/places/1/thumbnail"))
                .andExpect(jsonPath("$.result.element").value("수"))
                .andExpect(jsonPath("$.result.hashtags[0]").value("감정 회복"))
                .andExpect(jsonPath("$.result.description.question").value("이 터의 특징은 무엇인가요?"))
                .andExpect(jsonPath("$.result.description.answer").value("수(水) 기운이 강해 감정 정리와 회복에 좋고 오늘의 흐름과 잘 맞아요."))
                .andExpect(jsonPath("$.result.address").value("서울 중구 무교동"))
                .andExpect(jsonPath("$.result.latitude").value(37.5665))
                .andExpect(jsonPath("$.result.longitude").value(126.9780))
                .andExpect(jsonPath("$.result.mapUrl").value("https://map.example.com/places/1"))
                .andExpect(jsonPath("$.result.reviewCount").value(9))
                .andExpect(jsonPath("$.result.isSaved").value(false))
                .andExpect(jsonPath("$.result.isVisited").value(false));
    }

    @Test
    void getPlaceDetail_withoutAuthorizationHeader_returnsOkForGuest() throws Exception {
        when(placeDetailService.getPlaceDetail(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new PlaceDetailResponse(
                        1L,
                        "청계천 모전교",
                        null,
                        "수",
                        List.of("감정 회복"),
                        new PlaceDetailResponse.PlaceDescription(
                                "이 터의 특징은 무엇인가요?",
                                "수(水) 기운이 강해 감정 정리와 회복에 좋고 오늘의 흐름과 잘 맞아요."
                        ),
                        "서울 중구 무교동",
                        37.5665,
                        126.9780,
                        "https://map.example.com/places/1",
                        9L,
                        false,
                        false
                ));

        mockMvc.perform(get("/places/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.isSaved").value(false))
                .andExpect(jsonPath("$.result.isVisited").value(false));
    }

    @Test
    void getPlaceDetail_placeNotFound_returnsNotFound() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeDetailService.getPlaceDetail(org.mockito.ArgumentMatchers.eq(999L), org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new CustomException(PlaceErrorCode.PLACE_NOT_FOUND));

        mockMvc.perform(get("/places/999")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE404_1"));
    }

    @Test
    void updateBookmark_withoutAuthorizationHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/places/1/bookmark")
                        .contentType("application/json")
                        .content("{\"isSaved\":true}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void updateBookmark_withValidToken_returnsOk() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeService.updateBookmark(1L, true))
                .thenReturn(new PlaceBookmarkResponse(1L, true));

        mockMvc.perform(patch("/places/1/bookmark")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content("{\"isSaved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.placeId").value(1))
                .andExpect(jsonPath("$.result.isSaved").value(true));
    }

    @Test
    void getPlaceReviews_withoutAccessToken_returnsOkForGuest() throws Exception {
        when(placeDetailService.getPlaceReviews(1L))
                .thenReturn(new PlaceReviewListResponse(0, null, List.of()));

        mockMvc.perform(get("/places/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(0))
                .andExpect(jsonPath("$.result.reviews").isArray());
    }

    @Test
    void getPlaceReviews_returnsTotalCountMyReviewAndReviews() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeDetailService.getPlaceReviews(1L))
                .thenReturn(new PlaceReviewListResponse(
                        2,
                        new PlaceReviewItemResponse(
                                11L,
                                "나",
                                5,
                                "다시 생각해도 좋았어요",
                                List.of(),
                                LocalDateTime.of(2026, 7, 19, 11, 0)
                        ),
                        List.of(new PlaceReviewItemResponse(
                                10L,
                                "리뷰어",
                                4,
                                "좋았어요",
                                List.of(new ImageInfo(101L, "https://cdn/1.jpg")),
                                LocalDateTime.of(2026, 7, 19, 10, 0)
                        ))
                ));

        mockMvc.perform(get("/places/1/reviews")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(2))
                .andExpect(jsonPath("$.result.myReview.reviewId").value(11))
                .andExpect(jsonPath("$.result.reviews[0].reviewId").value(10))
                .andExpect(jsonPath("$.result.reviews[0].writerNickname").value("리뷰어"))
                .andExpect(jsonPath("$.result.reviews[0].rating").value(4))
                .andExpect(jsonPath("$.result.reviews[0].images[0].imageId").value(101))
                .andExpect(jsonPath("$.result.reviews[0].images[0].imageUrl").value("https://cdn/1.jpg"));
    }

    @Test
    void getPlaceReviews_noMyReview_returnsNullMyReview() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeDetailService.getPlaceReviews(1L))
                .thenReturn(new PlaceReviewListResponse(0, null, List.of()));

        mockMvc.perform(get("/places/1/reviews")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.myReview").doesNotExist())
                .andExpect(jsonPath("$.result.reviews").isEmpty());
    }

    @Test
    void getPlaceReviews_placeNotFound_returnsNotFound() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeDetailService.getPlaceReviews(999L))
                .thenThrow(new CustomException(PlaceErrorCode.PLACE_NOT_FOUND));

        mockMvc.perform(get("/places/999/reviews")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE404_1"));
    }

    @Test
    void getShareCard_withoutAccessToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/places/1/share-cards"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void getShareCard_returnsPlaceNameElementAndImageUrl() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeShareCardService.getShareCard(1L))
                .thenReturn(new PlaceShareCardResponse("청계천 모전교", "수", "https://cdn/place2.jpg"));

        mockMvc.perform(get("/places/1/share-cards")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.placeName").value("청계천 모전교"))
                .andExpect(jsonPath("$.result.element").value("수"))
                .andExpect(jsonPath("$.result.imageUrl").value("https://cdn/place2.jpg"));
    }

    @Test
    void getShareCard_placeNotFound_returnsNotFound() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeShareCardService.getShareCard(999L))
                .thenThrow(new CustomException(PlaceErrorCode.PLACE_NOT_FOUND));

        mockMvc.perform(get("/places/999/share-cards")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE404_1"));
    }

    @Test
    void getShareCard_noGooglePhoto_returnsNotFoundWithImageSpecificCode() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(placeShareCardService.getShareCard(1L))
                .thenThrow(new CustomException(PlaceErrorCode.PLACE_IMAGE_NOT_FOUND));

        mockMvc.perform(get("/places/1/share-cards")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE404_3"));
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
