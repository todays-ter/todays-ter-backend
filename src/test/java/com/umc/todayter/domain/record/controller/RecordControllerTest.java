package com.umc.todayter.domain.record.controller;

import com.umc.todayter.domain.record.dto.response.ImageInfo;
import com.umc.todayter.domain.record.dto.response.RecordDetailResponse;
import com.umc.todayter.domain.record.dto.response.RecordIdResponse;
import com.umc.todayter.domain.record.dto.response.RecordResponse;
import com.umc.todayter.domain.record.dto.response.RecordUpdateResponse;
import com.umc.todayter.domain.record.enums.RecordType;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
                {"placeId":1,"type":"RECORD","rating":4,"content":"좋았어요","imageIds":[]}
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
                1L, RecordType.RECORD, 1L, "북촌한옥마을", LocalDateTime.of(2026, 7, 1, 10, 0),
                4, "좋았어요", List.of(new ImageInfo(101L, "https://img1")), LocalDateTime.now()
        ));

        String body = """
                {"placeId":1,"type":"RECORD","rating":4,"content":"좋았어요","imageIds":[101]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.recordId").value(1))
                .andExpect(jsonPath("$.result.reviewId").doesNotExist())
                .andExpect(jsonPath("$.result.placeId").value(1))
                .andExpect(jsonPath("$.result.rating").value(4))
                .andExpect(jsonPath("$.result.images[0].imageId").value(101));
    }

    @Test
    void createRecord_withValidToken_reviewTypeReturnsReviewIdKey() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.createRecord(any())).thenReturn(new RecordResponse(
                2L, RecordType.REVIEW, 1L, "북촌한옥마을", LocalDateTime.of(2026, 7, 1, 10, 0),
                5, "좋았어요", List.of(), LocalDateTime.now()
        ));

        String body = """
                {"placeId":1,"type":"REVIEW","rating":5,"content":"좋았어요","imageIds":[]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.reviewId").value(2))
                .andExpect(jsonPath("$.result.recordId").doesNotExist());
    }

    @Test
    void createRecord_withValidToken_returnsNotFoundWhenPlaceMissing() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.createRecord(any())).thenThrow(new CustomException(RecordErrorCode.PLACE_NOT_FOUND));

        String body = """
                {"placeId":999,"type":"REVIEW","rating":5,"content":"좋았어요","imageIds":[]}
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
    void createRecord_withValidToken_returnsConflictWhenReviewAlreadyExists() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.createRecord(any())).thenThrow(new CustomException(RecordErrorCode.REVIEW_ALREADY_EXISTS));

        String body = """
                {"placeId":1,"type":"REVIEW","rating":5,"content":"좋았어요","imageIds":[]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("RECORD409_1"));
    }

    @Test
    void createRecord_withInvalidRating_returnsBadRequest() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);

        String body = """
                {"placeId":1,"type":"RECORD","rating":6,"content":"내용","imageIds":[]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRecord_withMissingRequiredFields_returnsBadRequest() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);

        String body = """
                {"placeId":null,"type":null,"rating":null,"content":"","imageIds":[]}
                """;

        mockMvc.perform(post("/records")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecordDetail_withoutAuthorizationHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/records/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void getRecordDetail_withValidToken_returnsOk() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.getRecordDetail(10L)).thenReturn(new RecordDetailResponse(
                10L, RecordType.RECORD, 2L, "청계천 모전교", null,
                4, "오늘의 기운이 좋았어요", List.of("https://cdn.../review1.jpg"),
                LocalDateTime.of(2026, 7, 19, 10, 0), LocalDateTime.of(2026, 7, 19, 10, 0)
        ));

        mockMvc.perform(get("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.recordId").value(10))
                .andExpect(jsonPath("$.result.reviewId").doesNotExist())
                .andExpect(jsonPath("$.result.placeId").value(2))
                .andExpect(jsonPath("$.result.imageUrls[0]").value("https://cdn.../review1.jpg"))
                .andExpect(jsonPath("$.result.visitVerifiedAt").doesNotExist());
    }

    @Test
    void getRecordDetail_reviewType_returnsReviewIdKey() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.getRecordDetail(10L)).thenReturn(new RecordDetailResponse(
                10L, RecordType.REVIEW, 2L, "청계천 모전교", null,
                4, "내용", List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));

        mockMvc.perform(get("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.reviewId").value(10))
                .andExpect(jsonPath("$.result.recordId").doesNotExist());
    }

    @Test
    void getRecordDetail_withValidToken_returnsNotFoundWhenMissing() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.getRecordDetail(10L)).thenThrow(new CustomException(RecordErrorCode.RECORD_NOT_FOUND));

        mockMvc.perform(get("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECORD404_3"));
    }

    @Test
    void getRecordDetail_withValidToken_returnsForbiddenWhenNotOwner() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.getRecordDetail(10L)).thenThrow(new CustomException(RecordErrorCode.RECORD_ACCESS_DENIED));

        mockMvc.perform(get("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RECORD403_2"));
    }

    @Test
    void updateRecord_withoutAuthorizationHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/records/10")
                        .contentType("application/json")
                        .content("{\"rating\":5}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void updateRecord_withValidToken_returnsOk() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.updateRecord(eq(10L), any())).thenReturn(new RecordUpdateResponse(
                10L, RecordType.RECORD, 5, "다시 생각해보니 최고였어요",
                List.of(new ImageInfo(101L, "https://cdn.../review1.jpg")),
                LocalDateTime.of(2026, 7, 19, 11, 0)
        ));

        String body = """
                {"rating":5,"content":"다시 생각해보니 최고였어요","imageIds":[101]}
                """;

        mockMvc.perform(patch("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.recordId").value(10))
                .andExpect(jsonPath("$.result.rating").value(5))
                .andExpect(jsonPath("$.result.content").value("다시 생각해보니 최고였어요"))
                .andExpect(jsonPath("$.result.images[0].imageId").value(101));
    }

    @Test
    void updateRecord_withInvalidRating_returnsBadRequest() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);

        mockMvc.perform(patch("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content("{\"rating\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRecord_withNoFields_returnsBadRequest() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.updateRecord(eq(10L), any())).thenThrow(new CustomException(RecordErrorCode.NO_UPDATE_FIELD));

        mockMvc.perform(patch("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RECORD400_5"));
    }

    @Test
    void updateRecord_returnsNotFoundWhenMissing() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.updateRecord(eq(10L), any())).thenThrow(new CustomException(RecordErrorCode.RECORD_NOT_FOUND));

        mockMvc.perform(patch("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content("{\"rating\":5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECORD404_3"));
    }

    @Test
    void updateRecord_returnsForbiddenWhenNotOwner() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.updateRecord(eq(10L), any())).thenThrow(new CustomException(RecordErrorCode.RECORD_ACCESS_DENIED));

        mockMvc.perform(patch("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content("{\"rating\":5}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RECORD403_2"));
    }

    @Test
    void deleteRecord_withoutAuthorizationHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/records/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @Test
    void deleteRecord_withValidToken_returnsOkWithIdOnly() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.deleteRecord(10L)).thenReturn(new RecordIdResponse(10L, RecordType.RECORD));

        mockMvc.perform(delete("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.recordId").value(10))
                .andExpect(jsonPath("$.result.rating").doesNotExist())
                .andExpect(jsonPath("$.result.content").doesNotExist());
    }

    @Test
    void deleteRecord_reviewType_returnsReviewIdKey() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.deleteRecord(10L)).thenReturn(new RecordIdResponse(10L, RecordType.REVIEW));

        mockMvc.perform(delete("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.reviewId").value(10))
                .andExpect(jsonPath("$.result.recordId").doesNotExist());
    }

    @Test
    void deleteRecord_returnsNotFoundWhenMissing() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.deleteRecord(10L)).thenThrow(new CustomException(RecordErrorCode.RECORD_NOT_FOUND));

        mockMvc.perform(delete("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECORD404_3"));
    }

    @Test
    void deleteRecord_returnsForbiddenWhenNotOwner() throws Exception {
        when(jwtProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getMemberId(VALID_TOKEN)).thenReturn(1L);
        when(recordService.deleteRecord(10L)).thenThrow(new CustomException(RecordErrorCode.RECORD_ACCESS_DENIED));

        mockMvc.perform(delete("/records/10")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RECORD403_2"));
    }
}
