package com.umc.todayter.domain.fortune.service.parser;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ContentBlock;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.DetailSection;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.DayPillarCards;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ElementDistribution;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.LabeledText;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.PillarCard;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class FortuneReportResultParser {

    private static final Map<String, String> STEM_READINGS = Map.of(
            "甲", "갑", "乙", "을", "丙", "병", "丁", "정", "戊", "무",
            "己", "기", "庚", "경", "辛", "신", "壬", "임", "癸", "계"
    );
    private static final Map<String, String> BRANCH_READINGS = Map.ofEntries(
            Map.entry("子", "자"), Map.entry("丑", "축"), Map.entry("寅", "인"),
            Map.entry("卯", "묘"), Map.entry("辰", "진"), Map.entry("巳", "사"),
            Map.entry("午", "오"), Map.entry("未", "미"), Map.entry("申", "신"),
            Map.entry("酉", "유"), Map.entry("戌", "술"), Map.entry("亥", "해")
    );

    private static final Pattern SECTION_PATTERN = Pattern.compile("^##\\s+(\\d+)\\.\\s*(.+?)\\s*$");
    private static final Pattern SUBSECTION_PATTERN = Pattern.compile("^###\\s+(.+?)\\s*$");
    private static final Pattern PRIMARY_ELEMENT_PATTERN = Pattern.compile("주\\s*오행\\s*\\d+\\s*:\\s*([목화토금수])");
    private static final Pattern COMPLEMENT_ELEMENT_PATTERN = Pattern.compile("보완\\s*오행\\s*:\\s*([목화토금수])");
    private static final Pattern DISTRIBUTION_PATTERN = Pattern.compile(
            "(?m)^\\s*-\\s*([목화토금수])\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%"
    );

    private static final Map<Integer, String> SECTION_CODES = Map.of(
            1, "GENERAL",
            2, "LOVE",
            3, "CAREER",
            4, "WEALTH",
            5, "RELATIONSHIP",
            6, "HEALTH"
    );

    private final ObjectMapper objectMapper;

    public ParsedReport parse(String markdown) {
        return parse(markdown, null);
    }

    public ParsedReport parse(String markdown, String manseData) {
        Map<Integer, SectionDraft> sections = parseSections(markdown);
        BasicReport basic = toBasicReport(sections);
        FiveElement complementElement = basic.complementElement();

        List<DetailSection> details = new ArrayList<>();
        for (int sectionNumber = 1; sectionNumber <= 6; sectionNumber++) {
            SectionDraft section = sections.get(sectionNumber);
            if (section != null) {
                details.add(toDetailSection(
                        sectionNumber,
                        section,
                        manseData,
                        basic.primaryElements()
                ));
            }
        }

        return new ParsedReport(basic, List.copyOf(details), complementElement);
    }

    public BasicReport parseBasic(String markdown) {
        return toBasicReport(parseSections(markdown));
    }

    private BasicReport toBasicReport(Map<Integer, SectionDraft> sections) {
        SectionDraft summarySection = sections.getOrDefault(0, SectionDraft.empty("기본 리포트 요약"));
        SectionDraft generalSection = sections.getOrDefault(1, SectionDraft.empty("종합"));

        List<FiveElement> primaryElements = parsePrimaryElements(summarySection.value("주 오행"));
        FiveElement complementElement = parseComplementElement(generalSection.value("보완 오행"));
        List<ElementDistribution> distribution = parseDistribution(summarySection.value("오행 분포"));
        List<LabeledText> tendencies = parseLabeledItems(generalSection.value("기본 흐름 분석"));

        return new BasicReport(
                clean(summarySection.value("유형 제목")),
                clean(summarySection.value("유형명")),
                createElementSummary(primaryElements, complementElement),
                primaryElements,
                complementElement,
                distribution,
                tendencies
        );
    }

    private Map<Integer, SectionDraft> parseSections(String markdown) {
        Map<Integer, SectionDraft> sections = new LinkedHashMap<>();
        if (markdown == null || markdown.isBlank()) {
            return sections;
        }

        Integer currentSectionNumber = null;
        SectionDraft currentSection = null;
        String currentSubsection = null;
        StringBuilder content = new StringBuilder();

        for (String rawLine : markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) {
            String line = rawLine.stripTrailing();
            Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
            Matcher subsectionMatcher = SUBSECTION_PATTERN.matcher(line);

            if (sectionMatcher.matches()) {
                flushSubsection(currentSection, currentSubsection, content);
                currentSectionNumber = Integer.parseInt(sectionMatcher.group(1));
                currentSection = new SectionDraft(sectionMatcher.group(2).trim());
                sections.put(currentSectionNumber, currentSection);
                currentSubsection = null;
                continue;
            }

            if (subsectionMatcher.matches() && currentSectionNumber != null) {
                flushSubsection(currentSection, currentSubsection, content);
                currentSubsection = subsectionMatcher.group(1).trim();
                continue;
            }

            if (currentSection != null && currentSubsection != null && !"---".equals(line.trim())) {
                content.append(line).append('\n');
            }
        }

        flushSubsection(currentSection, currentSubsection, content);
        return sections;
    }

    private void flushSubsection(SectionDraft section, String title, StringBuilder content) {
        if (section != null && title != null) {
            section.subsections.put(title, content.toString().trim());
        }
        content.setLength(0);
    }

    private DetailSection toDetailSection(
            int sectionNumber,
            SectionDraft section,
            String manseData,
            List<FiveElement> primaryElements
    ) {
        String coreSummary = clean(section.value("핵심 요약"));
        List<ContentBlock> blocks = new ArrayList<>();
        List<LabeledText> flowAnalysis = sectionNumber == 1
                ? parseLabeledItems(section.value("기본 흐름 분석"))
                : List.of();
        List<LabeledText> keyPoints = sectionNumber == 1
                ? List.of()
                : parseKeyPoints(section);
        DayPillarCards dayPillars = sectionNumber == 1 ? parseDayPillars(section, manseData) : null;

        for (Map.Entry<String, String> entry : section.subsections.entrySet()) {
            String title = entry.getKey();
            if ("핵심 요약".equals(title)
                    || "보완 오행".equals(title)
                    || title.endsWith("핵심 정리")
                    || (sectionNumber == 1 && isGeneralCardSection(title))) {
                continue;
            }
            blocks.add(new ContentBlock(title, clean(entry.getValue())));
        }

        return new DetailSection(
                SECTION_CODES.getOrDefault(sectionNumber, "SECTION_" + sectionNumber),
                section.title,
                coreSummary,
                sectionNumber == 1 ? primaryElements : List.of(),
                dayPillars,
                List.copyOf(blocks),
                flowAnalysis,
                keyPoints
        );
    }

    private List<LabeledText> parseKeyPoints(SectionDraft section) {
        return section.subsections.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("핵심 정리"))
                .findFirst()
                .map(entry -> parseLabeledItems(entry.getValue()))
                .orElseGet(List::of);
    }

    private boolean isGeneralCardSection(String title) {
        return "일간".equals(title)
                || "일지".equals(title)
                || "일주".equals(title)
                || "기본 흐름 분석".equals(title);
    }

    private DayPillarCards parseDayPillars(SectionDraft section, String manseData) {
        JsonNode day = readDayPillar(manseData);
        String hanja = day.path("hanja").asText(null);
        String hangul = day.path("hangul").asText(null);
        String stem = firstNonNull(day.path("heaven").asText(null), characterAt(hanja, 0));
        String branch = firstNonNull(day.path("earth").asText(null), characterAt(hanja, 1));
        if (stem == null && branch == null) {
            return null;
        }

        String stemReading = firstNonNull(characterAt(hangul, 0), stem == null ? null : STEM_READINGS.get(stem));
        String branchReading = firstNonNull(characterAt(hangul, 1), branch == null ? null : BRANCH_READINGS.get(branch));

        PillarCard dayStem = createPillarCard("일간", stem, stemReading, clean(section.value("일간")), true);
        PillarCard dayBranch = createPillarCard("일지", branch, branchReading, clean(section.value("일지")), true);
        PillarCard dayPillar = createPillarCard(
                "일주",
                join(stem, branch),
                join(stemReading, branchReading),
                clean(section.value("일주")),
                false
        );
        return new DayPillarCards(dayStem, dayBranch, dayPillar);
    }

    private JsonNode readDayPillar(String manseData) {
        if (manseData == null || manseData.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode root = objectMapper.readTree(manseData);
            if (root == null) {
                return objectMapper.createObjectNode();
            }
            JsonNode v2Day = root.path("day");
            return v2Day.isObject() ? v2Day : root.path("saju").path("day");
        } catch (JacksonException e) {
            throw new IllegalStateException("저장된 만세력의 일주 정보를 읽을 수 없습니다.", e);
        }
    }

    private String characterAt(String value, int index) {
        if (value == null || value.codePointCount(0, value.length()) <= index) {
            return null;
        }
        int start = value.offsetByCodePoints(0, index);
        int end = value.offsetByCodePoints(start, 1);
        return value.substring(start, end);
    }

    private String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private PillarCard createPillarCard(
            String label,
            String hanja,
            String korean,
            String description,
            boolean showReading
    ) {
        String displayText = hanja == null
                ? null
                : showReading && korean != null ? hanja + "(" + korean + ")" : hanja;
        return new PillarCard(label, displayText, description);
    }

    private String join(String first, String second) {
        return first == null || second == null ? null : first + second;
    }

    private String createElementSummary(List<FiveElement> primaryElements, FiveElement complementElement) {
        if (primaryElements.size() < 2 || complementElement == null) {
            return null;
        }
        String first = primaryElements.get(0).getLabel();
        String second = primaryElements.get(1).getLabel();
        String complement = complementElement.getLabel();
        return "당신은 " + first + coordinateParticle(first) + " " + second + "의 흐름이 강하고, "
                + complement + subjectParticle(complement) + " 부족한 편이에요.";
    }

    private String coordinateParticle(String word) {
        return hasFinalConsonant(word) ? "과" : "와";
    }

    private String subjectParticle(String word) {
        return hasFinalConsonant(word) ? "이" : "가";
    }

    private boolean hasFinalConsonant(String word) {
        char last = word.charAt(word.length() - 1);
        return last >= 0xAC00 && last <= 0xD7A3 && (last - 0xAC00) % 28 != 0;
    }

    private List<FiveElement> parsePrimaryElements(String value) {
        List<FiveElement> elements = new ArrayList<>();
        Matcher matcher = PRIMARY_ELEMENT_PATTERN.matcher(value == null ? "" : value);
        while (matcher.find()) {
            FiveElement element = FiveElement.fromLabel(matcher.group(1));
            if (element != null) {
                elements.add(element);
            }
        }
        return List.copyOf(elements);
    }

    private FiveElement parseComplementElement(String value) {
        Matcher matcher = COMPLEMENT_ELEMENT_PATTERN.matcher(value == null ? "" : value);
        return matcher.find() ? FiveElement.fromLabel(matcher.group(1)) : null;
    }

    private List<ElementDistribution> parseDistribution(String value) {
        List<ElementDistribution> distribution = new ArrayList<>();
        Matcher matcher = DISTRIBUTION_PATTERN.matcher(value == null ? "" : value);
        while (matcher.find()) {
            FiveElement element = FiveElement.fromLabel(matcher.group(1));
            if (element != null) {
                distribution.add(new ElementDistribution(
                        element,
                        element.getLabel(),
                        new BigDecimal(matcher.group(2))
                ));
            }
        }
        return List.copyOf(distribution);
    }

    private List<LabeledText> parseLabeledItems(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        List<LabeledText> items = new ArrayList<>();
        for (String line : value.split("\\R")) {
            String normalized = line.trim();
            if (!normalized.startsWith("-")) {
                continue;
            }
            normalized = normalized.substring(1).trim().replace("**", "");
            int delimiter = normalized.indexOf(':');
            if (delimiter > 0) {
                items.add(new LabeledText(
                        normalized.substring(0, delimiter).trim(),
                        normalized.substring(delimiter + 1).trim()
                ));
            }
        }
        return List.copyOf(items);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replace("**", "").replaceAll("[ \\t]+(?=\\n)", "");
        return cleaned.isBlank() ? null : cleaned;
    }

    public record ParsedReport(
            BasicReport basic,
            List<DetailSection> details,
            FiveElement complementElement
    ) {
    }

    private static final class SectionDraft {
        private final String title;
        private final LinkedHashMap<String, String> subsections = new LinkedHashMap<>();

        private SectionDraft(String title) {
            this.title = title;
        }

        private static SectionDraft empty(String title) {
            return new SectionDraft(title);
        }

        private String value(String subsection) {
            return subsections.get(subsection);
        }
    }
}
