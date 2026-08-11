package com.umc.todayter.domain.fortune.service;

import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneReportResultParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FortuneReportResultParser parser = new FortuneReportResultParser(objectMapper);

    @Test
    void parsesSummaryAndDetailSections() {
        String markdown = """
                ## 0. 기본 리포트 요약

                ### 유형 제목
                변화에 유연한 직관형

                ### 유형명
                탐구자형

                ### 주 오행
                주 오행 1: 토
                주 오행 2: 금

                ### 오행 분포
                - 목: 1.3%
                - 화: 15.2%
                - 토: 43.8%
                - 금: 17.8%
                - 수: 11.6%

                ## 1. 종합

                ### 핵심 요약
                변화에 잘 적응합니다.

                ### 일간
                병화의 성격은 열정적입니다.

                ### 일지
                신금은 내면을 나타냅니다.

                ### 일주
                병신의 조합을 나타냅니다.

                ### 보완 오행
                보완 오행: 목

                ### 기본 흐름 분석
                - **감정 흐름**: 감정을 깊게 느낍니다.
                - **행동 스타일**: 직관적으로 결정합니다.

                ### 종합 핵심 정리
                - 핵심 강점: 뛰어난 직관력

                ## 2. 연애

                ### 핵심 요약
                감정의 깊이를 중요시합니다.

                ### 사랑을 시작하는 방식
                관찰 후 행동합니다.

                ### 연애 핵심 정리
                - 강점: 상대의 감정을 세심하게 살필 수 있다.
                - 주의점: 불안을 혼자 키우지 않도록 주의해야 한다.
                """;
        String manseData = """
                {"day":{"hangul":"병신","hanja":"丙申","stem_element":"fire"}}
                """;

        FortuneReportResultParser.ParsedReport result = parser.parse(markdown, manseData);

        assertThat(result.basic().typeTitle()).isEqualTo("변화에 유연한 직관형");
        assertThat(result.basic().typeName()).isEqualTo("탐구자형");
        assertThat(result.basic().elementSummary())
                .isEqualTo("당신은 토와 금의 흐름이 강하고, 목이 부족한 편이에요.");
        assertThat(result.basic().primaryElements()).containsExactly(FiveElement.EARTH, FiveElement.METAL);
        assertThat(result.basic().complementElement()).isEqualTo(FiveElement.WOOD);
        assertThat(result.basic().elementDistribution()).hasSize(5);
        assertThat(result.basic().elementDistribution().get(0).percentage())
                .isEqualByComparingTo(new BigDecimal("1.3"));
        assertThat(result.basic().overallTendencies()).extracting("label")
                .containsExactly("감정 흐름", "행동 스타일");

        assertThat(result.details()).hasSize(2);
        var general = result.details().get(0);
        assertThat(general.coreSummary()).isEqualTo("변화에 잘 적응합니다.");
        assertThat(general.primaryElements()).containsExactly(FiveElement.EARTH, FiveElement.METAL);
        var generalJson = objectMapper.valueToTree(general);
        assertThat(generalJson.has("code")).isFalse();
        assertThat(generalJson.has("title")).isFalse();
        assertThat(general.dayPillars().dayStem().displayText()).isEqualTo("丙(병)");
        assertThat(general.dayPillars().dayBranch().displayText()).isEqualTo("申(신)");
        assertThat(general.dayPillars().dayPillar().displayText()).isEqualTo("丙申");
        assertThat(general.contentBlocks()).isEmpty();
        assertThat(general.flowAnalysis()).extracting("label")
                .containsExactly("감정 흐름", "행동 스타일");
        assertThat(general.keyPoints()).extracting("text")
                .containsExactly("뛰어난 직관력");

        var love = result.details().get(1);
        assertThat(love.primaryElements()).isEmpty();
        assertThat(love.dayPillars()).isNull();
        assertThat(love.contentBlocks()).extracting("title")
                .containsExactly("사랑을 시작하는 방식");
        assertThat(love.keyPoints()).extracting("text")
                .containsExactly(
                        "상대의 감정을 세심하게 살필 수 있다.",
                        "불안을 혼자 키우지 않도록 주의해야 한다."
                );
    }

    @Test
    void parsesSummaryWithoutFailingWhenDayPillarDataIsMissing() {
        String markdown = """
                ## 0. 기본 리포트 요약

                ### 유형 제목
                변화에 유연한 직관형

                ### 유형명
                탐구자형

                ### 주 오행
                주 오행 1: 수
                주 오행 2: 목

                ## 1. 종합

                ### 핵심 요약
                변화에 유연하게 대응합니다.

                ### 보완 오행
                보완 오행: 화
                """;

        FortuneReportResultParser.ParsedReport result = parser.parse(markdown, "{}");

        assertThat(result.basic().typeName()).isEqualTo("탐구자형");
        assertThat(result.details().get(0).dayPillars()).isNull();
    }

    @Test
    void parsesBasicReportWithoutReadingManseData() {
        String markdown = """
                ## 0. 기본 리포트 요약

                ### 유형명
                탐구자형
                """;

        assertThat(parser.parseBasic(markdown).typeName()).isEqualTo("탐구자형");
    }
}
