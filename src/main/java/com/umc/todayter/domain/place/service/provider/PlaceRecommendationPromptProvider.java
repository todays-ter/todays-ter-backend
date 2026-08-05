package com.umc.todayter.domain.place.service.provider;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.LabeledText;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.entity.Place;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class PlaceRecommendationPromptProvider {

    private static final String PROMPT_RESOURCE = "classpath:prompts/place-recommendation.txt";
    private final String basePrompt;

    public PlaceRecommendationPromptProvider(ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(PROMPT_RESOURCE);
        try {
            this.basePrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("장소 추천 프롬프트를 읽을 수 없습니다.", e);
        }
    }

    public String create(RecommendationMatchContext match, Place place) {
        String primaryElements = match.basicReport().primaryElements().stream()
                .map(element -> element.getLabel())
                .collect(Collectors.joining(", "));
        String tendencies = match.basicReport().overallTendencies().stream()
                .map(this::formatTendency)
                .collect(Collectors.joining(" / "));

        String input = """
                [사용자와 오늘의 흐름]
                주오행: %s
                필요오행: %s
                고민 유형: %s
                사주 리포트 성향: %s
                오늘 일진 오행: %s

                [장소]
                장소명: %s
                장소 대표오행: %s
                장소 지형·유형: %s
                장소 요약: %s
                장소 설명: %s

                [검증된 매칭 결과]
                오행 일치도: %d/45
                고민 유형 적합도: %d/30
                일진 가중치: %d/25
                최종 매칭 점수: %d/100
                """.formatted(
                primaryElements,
                match.neededElement().getDisplayName(),
                match.concerns(),
                tendencies,
                match.dailyElement().getDisplayName(),
                place.getName(),
                place.getElementType().getDisplayName(),
                place.getTerrainType(),
                place.getSummary(),
                place.getDescription(),
                match.elementScore(),
                match.concernScore(),
                match.dailyScore(),
                match.totalScore()
        );
        return basePrompt.replace("{{PLACE_RECOMMENDATION_INPUT}}", input);
    }

    private String formatTendency(LabeledText tendency) {
        return tendency.label() + ": " + tendency.text();
    }
}
