package com.umc.todayter.domain.fortune.service;

import tools.jackson.databind.JsonNode;
import com.umc.todayter.domain.fortune.config.OpenAiReportProperties;
import com.umc.todayter.domain.fortune.dto.internal.FortuneReportGenerationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FortuneReportPromptProvider {

    private final String basePrompt;
    public FortuneReportPromptProvider(
            ResourceLoader resourceLoader,
            OpenAiReportProperties properties
    ) {
        Resource resource = resourceLoader.getResource(properties.promptResource());
        try {
            this.basePrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("사주 리포트 프롬프트를 읽을 수 없습니다.", e);
        }
    }

    public String create(FortuneReportGenerationContext context, JsonNode manseData) {
        String input = """
                성별: %s
                달력 유형: %s
                생년월일: %s
                출생 시각: %s
                출생 시각 모름: %s
                고민 유형: %s

                계산된 만세력 정보:
                %s
                """.formatted(
                context.gender(),
                context.calendarType(),
                context.birthDate(),
                context.birthTimeUnknown() ? "제공되지 않음" : context.birthTime(),
                context.birthTimeUnknown(),
                context.concernTypes(),
                manseData.toPrettyString()
        );
        return basePrompt.replace("{{FORTUNE_REPORT_INPUT}}", input);
    }
}
