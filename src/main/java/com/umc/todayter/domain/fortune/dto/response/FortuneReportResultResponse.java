package com.umc.todayter.domain.fortune.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.todayter.domain.fortune.enums.FiveElement;

import java.math.BigDecimal;
import java.util.List;

public final class FortuneReportResultResponse {

    private FortuneReportResultResponse() {
    }

    public record BasicReport(
            String typeTitle,
            String typeName,
            String elementSummary,
            List<FiveElement> primaryElements,
            FiveElement complementElement,
            List<ElementDistribution> elementDistribution,
            List<LabeledText> overallTendencies
    ) {
    }

    public record ElementDistribution(
            FiveElement element,
            String label,
            BigDecimal percentage
    ) {
    }

    public record DetailSection(
            String code,
            String title,
            String coreSummary,
            @JsonInclude(JsonInclude.Include.NON_NULL) DayPillarCards dayPillars,
            @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ContentBlock> contentBlocks,
            @JsonInclude(JsonInclude.Include.NON_EMPTY) List<LabeledText> flowAnalysis,
            @JsonInclude(JsonInclude.Include.NON_EMPTY) List<LabeledText> keyPoints
    ) {
    }

    public record DayPillarCards(
            PillarCard dayStem,
            PillarCard dayBranch,
            PillarCard dayPillar
    ) {
    }

    public record PillarCard(
            String label,
            String displayText,
            String description
    ) {
    }

    public record ContentBlock(String title, String content) {
    }

    public record LabeledText(String label, String text) {
    }

    public record ComplementActionGuide(
            FiveElement element,
            String label,
            List<ActionItem> actions
    ) {
    }

    public record ActionItem(int order, String type, String text) {
    }
}
