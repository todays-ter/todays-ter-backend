package com.umc.todayter.domain.record.controller;

import com.umc.todayter.domain.record.dto.response.RecordResponse;
import com.umc.todayter.domain.record.exception.RecordErrorCode;
import com.umc.todayter.domain.record.service.RecordService;
import com.umc.todayter.global.apiPayload.exception.CustomException;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecordController.class)
@Import({
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        SecurityConfig.class
})
class RecordControllerTest {

    private static final String VALID_TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordService recordService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createRecord_withoutAuthorizationHeader_returnsUnauthorized() throws Exception {
        String body = """
                {"placeId":1,"content":"좋았어요","visitedAt":"2026-07-01","imageUrls":[]}
                """;

        mockMvc.perform(post("/records")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void createRecord_withValidToken_returnsCreated() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.createRecord(any())).thenReturn(new RecordResponse(
                1L, 1L, "북촌한옥마을", LocalDate.of(2026, 7, 1), "좋았어요", List.of("https://img1")
        ));

        String body = """
                {"placeId":1,"content":"좋았어요","visitedAt":"2026-07-01","imageUrls":["https://img1"]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.placeId").value(1))
                .andExpect(jsonPath("$.result.imageUrls[0]").value("https://img1"));
    }

    @Test
    void createRecord_withValidToken_returnsNotFoundWhenPlaceMissing() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.createRecord(any())).thenThrow(new CustomException(RecordErrorCode.PLACE_NOT_FOUND));

        String body = """
                {"placeId":999,"content":"좋았어요","visitedAt":"2026-07-01","imageUrls":[]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("RECORD404_1"));
    }

    @Test
    void createRecord_withInvalidBody_returnsBadRequest() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);

        String body = """
                {"placeId":null,"content":"","visitedAt":"2099-01-01","imageUrls":[]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
