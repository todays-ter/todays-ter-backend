package com.umc.todayter.domain.fortune.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ActionItem;
import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ComplementActionGuide;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ComplementActionProvider {

    private static final String RESOURCE_PATH = "fortune/complement-actions.tsv";
    private final Map<FiveElement, LinkedHashMap<String, List<String>>> actionsByElement;

    public ComplementActionProvider() {
        this.actionsByElement = loadActions();
    }

    public ComplementActionGuide select(FiveElement element, Long reportId) {
        if (element == null) {
            return new ComplementActionGuide(null, null, List.of());
        }

        LinkedHashMap<String, List<String>> grouped = actionsByElement.get(element);
        if (grouped == null || grouped.isEmpty()) {
            return new ComplementActionGuide(element, element.getLabel(), List.of());
        }

        List<Map.Entry<String, List<String>>> groups = new ArrayList<>(grouped.entrySet());
        int seed = Math.floorMod(Long.hashCode(reportId == null ? 0L : reportId), groups.size());
        List<ActionItem> selected = new ArrayList<>();

        for (int index = 0; index < Math.min(3, groups.size()); index++) {
            Map.Entry<String, List<String>> group = groups.get((seed + index) % groups.size());
            List<String> messages = group.getValue();
            int messageIndex = Math.floorMod(seed + index, messages.size());
            selected.add(new ActionItem(index + 1, group.getKey(), messages.get(messageIndex)));
        }

        return new ComplementActionGuide(element, element.getLabel(), List.copyOf(selected));
    }

    private Map<FiveElement, LinkedHashMap<String, List<String>>> loadActions() {
        Map<FiveElement, LinkedHashMap<String, List<String>>> loaded = new LinkedHashMap<>();
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )) {
            reader.lines()
                    .filter(line -> !line.isBlank())
                    .forEach(line -> addLine(loaded, line));
        } catch (IOException e) {
            throw new IllegalStateException("보완 오행 행동 라이팅을 읽을 수 없습니다.", e);
        }

        return loaded;
    }

    private void addLine(Map<FiveElement, LinkedHashMap<String, List<String>>> target, String line) {
        String[] columns = line.split("\\t", 3);
        if (columns.length != 3) {
            throw new IllegalStateException("잘못된 보완 오행 행동 라이팅 형식입니다: " + line);
        }

        FiveElement element = FiveElement.fromLabel(columns[0]);
        if (element == null) {
            throw new IllegalStateException("지원하지 않는 오행입니다: " + columns[0]);
        }

        target.computeIfAbsent(element, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(columns[1], ignored -> new ArrayList<>())
                .add(columns[2]);
    }
}
