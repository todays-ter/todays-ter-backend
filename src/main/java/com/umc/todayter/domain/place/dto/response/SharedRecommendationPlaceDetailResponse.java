package com.umc.todayter.domain.place.dto.response;

import java.util.List;

public record SharedRecommendationPlaceDetailResponse(
        String sharerNickname,
        Long placeId,
        String placeName,
        String imageUrl,
        PlaceSearchTypeResponse primaryElement,
        List<String> topCategories,
        Integer matchingScore,
        List<String> matchingPoints,
        String whyItMatches,
        String actionSuggestion,
        String mapUrl,
        Boolean isSaved
) {
    public static SharedRecommendationPlaceDetailResponse from(
            RecommendationPlaceDetailResponse detail,
            String sharerNickname
    ) {
        return new SharedRecommendationPlaceDetailResponse(
                sharerNickname, detail.placeId(), detail.placeName(), detail.imageUrl(),
                detail.primaryElement(), detail.topCategories(), detail.matchingScore(),
                detail.matchingPoints(), detail.whyItMatches(), detail.actionSuggestion(),
                detail.mapUrl(), detail.isSaved()
        );
    }
}
