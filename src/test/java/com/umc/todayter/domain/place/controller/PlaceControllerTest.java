package com.umc.todayter.domain.place.controller;

import com.umc.todayter.domain.place.dto.response.ElementFilterResponse;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.RegionFilterResponse;
import com.umc.todayter.domain.place.dto.response.ThemeFilterResponse;
import com.umc.todayter.domain.place.service.PlaceService;
import com.umc.todayter.global.config.SecurityConfig;
import com.umc.todayter.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlaceController.class)
@Import({
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        SecurityConfig.class
})
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

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
}
