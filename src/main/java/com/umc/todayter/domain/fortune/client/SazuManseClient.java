package com.umc.todayter.domain.fortune.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.todayter.domain.fortune.config.SazuApiProperties;
import com.umc.todayter.domain.fortune.dto.internal.FortuneReportGenerationContext;
import com.umc.todayter.domain.fortune.exception.FortuneReportGenerationException;
import com.umc.todayter.domain.onboarding.enums.CalendarType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SazuManseClient {

    private final RestClient restClient;
    private final SazuApiProperties properties;

    public SazuManseClient(
            @Qualifier("sazuRestClient") RestClient restClient,
            SazuApiProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public JsonNode calculate(FortuneReportGenerationContext context) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new FortuneReportGenerationException("SAZU_API_KEY_MISSING", "만세력 연동 설정을 확인해 주세요.");
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("birthYear", context.birthDate().getYear());
        request.put("birthMonth", context.birthDate().getMonthValue());
        request.put("birthDay", context.birthDate().getDayOfMonth());
        request.put("isLunar", context.calendarType() == CalendarType.LUNAR);
        request.put("modules", List.of("fourPillars", "elements"));

        if (!context.birthTimeUnknown() && context.birthTime() != null) {
            request.put("birthHour", context.birthTime().getHour());
            request.put("birthMinute", context.birthTime().getMinute());
        }

        try {
            JsonNode response = restClient.post()
                    .uri("/v1/sazu/calculate")
                    .header("x-api-key", properties.apiKey())
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.path("success").asBoolean(false) || response.path("data").isMissingNode()) {
                throw new FortuneReportGenerationException("SAZU_INVALID_RESPONSE", "만세력 정보를 생성하지 못했습니다.");
            }
            return response.path("data");
        } catch (FortuneReportGenerationException e) {
            throw e;
        } catch (RestClientException e) {
            throw new FortuneReportGenerationException(
                    "SAZU_API_FAILED",
                    "만세력 정보를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.",
                    e
            );
        }
    }
}
