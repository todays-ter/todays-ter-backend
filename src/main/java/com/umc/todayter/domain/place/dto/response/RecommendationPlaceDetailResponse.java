package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public record RecommendationPlaceDetailResponse(
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
    public static RecommendationPlaceDetailResponse from(
            Place place,
            boolean isSaved,
            PlaceRecommendationSnapshot snapshot,
            String contextPathUrl
    ) {
        return new RecommendationPlaceDetailResponse(
                place.getId(),
                place.getName(),
                thumbnailUrl(place, contextPathUrl),
                new PlaceSearchTypeResponse(
                        place.getElementType().name(),
                        place.getElementType().getDisplayName()
                ),
                PlaceListItemResponse.from(place, null).categories(),
                snapshot == null ? null : snapshot.getMatchingScore(),
                snapshot == null ? List.of() : snapshot.getMatchingPoints(),
                snapshot == null ? null : snapshot.getWhyItMatches(),
                snapshot == null ? null : snapshot.getActionSuggestion(),
                place.getMapUrl(),
                isSaved
        );
    }

    private static String thumbnailUrl(Place place, String contextPathUrl) {
        if (!StringUtils.hasText(place.getGooglePlaceId())) {
            return null;
        }
        return UriComponentsBuilder.fromUriString(contextPathUrl)
                .path("/places/{placeId}/thumbnail")
                .build(place.getId())
                .toString();
    }
}
