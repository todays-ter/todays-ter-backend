package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.place.entity.Place;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record PlaceListItemResponse(
        Long placeId,
        String placeName,
        String thumbnailUrl,
        List<String> categories,
        LocalDate savedDate,
        String element
) {
    private static final int TOP_CATEGORY_COUNT = 2;

    public static PlaceListItemResponse from(Place place, LocalDate savedDate) {
        return new PlaceListItemResponse(
                place.getId(),
                place.getName(),
                "/places/%d/thumbnail".formatted(place.getId()),
                topCategories(place),
                savedDate,
                place.getElementType().getDisplayName()
        );
    }

    private static List<String> topCategories(Place place) {
        Map<String, Integer> scoresByLabel = new java.util.LinkedHashMap<>();
        scoresByLabel.put("연애", place.getLoveScore());
        scoresByLabel.put("관계", place.getRelationshipScore());
        scoresByLabel.put("일·커리어", place.getCareerScore());
        scoresByLabel.put("학업", place.getStudyScore());
        scoresByLabel.put("휴식·회복", place.getRestScore());
        scoresByLabel.put("전환·자기정리", place.getTransitionScore());

        return scoresByLabel.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .limit(TOP_CATEGORY_COUNT)
                .map(Map.Entry::getKey)
                .toList();
    }
}
