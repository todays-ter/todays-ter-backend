package com.umc.todayter.domain.place.service.provider;

import com.umc.todayter.domain.place.dto.internal.PlaceRecommendationAiContent;
import com.umc.todayter.domain.fortune.exception.FortuneReportGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PlaceRecommendationAiContentParser {

    private final ObjectMapper objectMapper;

    public PlaceRecommendationAiContent parse(String output) {
        try {
            JsonNode root = objectMapper.readTree(stripCodeFence(output));
            String whyItMatches = root.path("whyItMatches").asText(null);
            String actionSuggestion = root.path("actionSuggestion").asText(null);
            if (!StringUtils.hasText(whyItMatches) || !StringUtils.hasText(actionSuggestion)) {
                throw invalidResponse();
            }
            return new PlaceRecommendationAiContent(whyItMatches.trim(), actionSuggestion.trim());
        } catch (JacksonException | IllegalArgumentException e) {
            throw invalidResponse();
        }
    }

    private String stripCodeFence(String output) {
        if (!StringUtils.hasText(output)) {
            throw invalidResponse();
        }
        String trimmed = output.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        int closingFence = trimmed.lastIndexOf("```");
        if (firstLineEnd < 0 || closingFence <= firstLineEnd) {
            throw invalidResponse();
        }
        return trimmed.substring(firstLineEnd + 1, closingFence).trim();
    }

    private FortuneReportGenerationException invalidResponse() {
        return new FortuneReportGenerationException(
                "OPENAI_INVALID_PLACE_RECOMMENDATION",
                "장소 맞춤 추천 문구를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요."
        );
    }
}
