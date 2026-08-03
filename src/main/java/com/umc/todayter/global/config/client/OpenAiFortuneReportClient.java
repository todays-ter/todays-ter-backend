package com.umc.todayter.global.config.client;

import tools.jackson.databind.JsonNode;
import com.umc.todayter.global.config.ablecityProperties.OpenAiReportProperties;
import com.umc.todayter.domain.fortune.exception.FortuneReportGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
@Slf4j
public class OpenAiFortuneReportClient {

    private final RestClient restClient;
    private final OpenAiReportProperties properties;

    public OpenAiFortuneReportClient(
            @Qualifier("openAiReportRestClient") RestClient restClient,
            OpenAiReportProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String generate(String prompt) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new FortuneReportGenerationException("OPENAI_API_KEY_MISSING", "AI 연동 설정을 확인해 주세요.");
        }
        if (!StringUtils.hasText(properties.model())) {
            throw new FortuneReportGenerationException("OPENAI_MODEL_MISSING", "AI 연동 설정을 확인해 주세요.");
        }

        Map<String, Object> request = Map.of(
                "model", properties.model(),
                "input", prompt,
                "store", false
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/v1/responses")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            String outputText = extractOutputText(response);
            if (!StringUtils.hasText(outputText)) {
                throw new FortuneReportGenerationException("OPENAI_INVALID_RESPONSE", "AI 리포트 결과를 확인할 수 없습니다.");
            }
            return outputText;
        } catch (FortuneReportGenerationException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn(
                    "OpenAI API 요청 실패. status={}",
                    e.getStatusCode()
            );
            throw new FortuneReportGenerationException(
                    "OPENAI_API_FAILED",
                    "AI 리포트를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.",
                    e
            );
        } catch (RestClientException e) {
            log.warn("OpenAI API 통신 실패. exception={}", e.getClass().getSimpleName());
            throw new FortuneReportGenerationException(
                    "OPENAI_API_FAILED", "AI 리포트를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.", e
            );
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            return null;
        }
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {
                    return content.path("text").asText(null);
                }
            }
        }
        return null;
    }
}
