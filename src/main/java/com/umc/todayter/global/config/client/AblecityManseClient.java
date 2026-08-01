package com.umc.todayter.global.config.client;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import com.umc.todayter.global.config.ablecityProperties.AblecityApiProperties;
import com.umc.todayter.domain.fortune.dto.internal.FortuneReportGenerationContext;
import com.umc.todayter.domain.fortune.exception.FortuneReportGenerationException;
import com.umc.todayter.domain.onboarding.enums.CalendarType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component
public class AblecityManseClient {

    private static final DateTimeFormatter BIRTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestClient restClient;
    private final AblecityApiProperties properties;

    public AblecityManseClient(
            @Qualifier("ablecityRestClient") RestClient restClient,
            AblecityApiProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public JsonNode calculate(FortuneReportGenerationContext context) {
        validate(context);

        boolean birthTimeUnknown = context.birthTimeUnknown() || context.birthTime() == null;
        LocalTime birthTime = birthTimeUnknown ? LocalTime.NOON : context.birthTime();
        String birth = LocalDateTime.of(context.birthDate(), birthTime).format(BIRTH_FORMATTER);
        String calendar = context.calendarType().name().toLowerCase(Locale.ROOT);

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v2/saju/fortune")
                            .queryParam("birth", birth)
                            .queryParam("gender", context.gender().toAblecityValue())
                            .queryParam("calendar", calendar)
                            .queryParam("midnightType", 0)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null
                    || !"success".equalsIgnoreCase(response.path("status").asText())
                    || response.path("data").isMissingNode()) {
                String status = response == null ? "NO_RESPONSE" : response.path("status").asText("UNKNOWN");
                String message = response == null ? "empty response" : response.path("message").asText("missing data");
                log.warn("Ablecity API returned an invalid response. status={}, message={}", status, message);
                throw new FortuneReportGenerationException(
                        "ABLECITY_INVALID_RESPONSE",
                        "만세력 정보를 생성하지 못했습니다."
                );
            }

            JsonNode data = response.path("data").deepCopy();
            if (birthTimeUnknown) {
                removeUnknownBirthTimeFields(data);
            }
            return data;
        } catch (FortuneReportGenerationException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn(
                    "Ablecity API request failed. status={}, response={}",
                    e.getStatusCode(),
                    abbreviate(e.getResponseBodyAsString(), 1_000)
            );
            throw new FortuneReportGenerationException(
                    "ABLECITY_API_FAILED",
                    "만세력 정보를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.",
                    e
            );
        } catch (RestClientException e) {
            log.warn("Ablecity API communication failed: {}", e.getMessage());
            throw new FortuneReportGenerationException(
                    "ABLECITY_API_FAILED",
                    "만세력 정보를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.",
                    e
            );
        }
    }

    private void validate(FortuneReportGenerationContext context) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new FortuneReportGenerationException(
                    "ABLECITY_API_KEY_MISSING",
                    "만세력 연동 설정을 확인해 주세요."
            );
        }
        if (context.gender() == null) {
            throw new FortuneReportGenerationException(
                    "ABLECITY_GENDER_REQUIRED",
                    "성별 정보가 필요합니다. 사주 정보를 다시 입력해 주세요."
            );
        }
        if (context.calendarType() != CalendarType.SOLAR && context.calendarType() != CalendarType.LUNAR) {
            throw new FortuneReportGenerationException(
                    "ABLECITY_CALENDAR_TYPE_INVALID",
                    "지원하지 않는 양력/음력 유형입니다."
            );
        }
    }

    private void removeUnknownBirthTimeFields(JsonNode data) {
        if (data.isObject()) {
            ((ObjectNode) data).put("birth_time_unknown", true);
        }
        JsonNode saju = data.path("saju");
        if (saju.isObject()) {
            ((ObjectNode) saju).remove("hour");
        }
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
