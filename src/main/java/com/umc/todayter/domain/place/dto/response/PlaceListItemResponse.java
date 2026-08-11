package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.place.entity.Place;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record PlaceListItemResponse(
        Long placeId,
        String placeName,
        String thumbnailUrl,
        List<String> categories,
        LocalDate savedDate,
        String element,
        Long recordId
) {
    public static PlaceListItemResponse from(Place place, LocalDate savedDate) {
        return from(place, savedDate, null);
    }

    public static PlaceListItemResponse from(Place place, LocalDate savedDate, Long recordId) {
        return new PlaceListItemResponse(
                place.getId(),
                place.getName(),
                "/places/%d/thumbnail".formatted(place.getId()),
                topCategories(place),
                savedDate,
                place.getElementType().getDisplayName(),
                recordId
        );
    }

    // 1등은 항상 반환하고, 2등은 3등과 점수가 같아 "2등"이 모호할 때 제외한다 (동점이면 1개만 반환)
    private static List<String> topCategories(Place place) {
        Map<String, Integer> scoresByLabel = new java.util.LinkedHashMap<>();
        scoresByLabel.put("연애", place.getLoveScore());
        scoresByLabel.put("관계", place.getRelationshipScore());
        scoresByLabel.put("일·커리어", place.getCareerScore());
        scoresByLabel.put("학업", place.getStudyScore());
        scoresByLabel.put("휴식·회복", place.getRestScore());
        scoresByLabel.put("전환·자기정리", place.getTransitionScore());

        List<Map.Entry<String, Integer>> sorted = scoresByLabel.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .toList();

        List<String> result = new ArrayList<>();
        result.add(sorted.get(0).getKey());
        if (!sorted.get(1).getValue().equals(sorted.get(2).getValue())) {
            result.add(sorted.get(1).getKey());
        }
        return result;
    }
}
