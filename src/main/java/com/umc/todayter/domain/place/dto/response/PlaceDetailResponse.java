package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record PlaceDetailResponse(
        Long placeId,
        String placeName,
        String imageUrl,
        String element,
        List<String> hashtags,
        PlaceDescription description,
        String address,
        Double latitude,
        Double longitude,
        String mapUrl,
        Long reviewCount,
        Boolean isSaved,
        Boolean isVisited
) {
    private static final String THUMBNAIL_PATH = "/places/{placeId}/thumbnail";
    private static final String DESCRIPTION_QUESTION = "이 터의 특징은 무엇인가요?";

    private static final Map<ElementType, String> ELEMENT_DESCRIPTIONS = new EnumMap<>(ElementType.class);

    static {
        ELEMENT_DESCRIPTIONS.put(ElementType.FIRE, "화(火) 기운이 강해 열정과 추진력이 필요한 새로운 시작에 좋고 오늘의 흐름과 잘 맞아요.");
        ELEMENT_DESCRIPTIONS.put(ElementType.EARTH, "토(土) 기운이 강해 안정감과 중심을 잡는 데 좋고 오늘의 흐름과 잘 맞아요.");
        ELEMENT_DESCRIPTIONS.put(ElementType.WOOD, "목(木) 기운이 강해 성장과 새로운 관계를 넓히는 데 좋고 오늘의 흐름과 잘 맞아요.");
        ELEMENT_DESCRIPTIONS.put(ElementType.WATER, "수(水) 기운이 강해 감정 정리와 회복에 좋고 오늘의 흐름과 잘 맞아요.");
        ELEMENT_DESCRIPTIONS.put(ElementType.METAL, "금(金) 기운이 강해 결단과 정리에 좋고 오늘의 흐름과 잘 맞아요.");
    }

    public static PlaceDetailResponse from(
            Place place,
            String contextPathUrl,
            long reviewCount,
            boolean isSaved,
            boolean isVisited
    ) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                thumbnailUrl(place, contextPathUrl),
                place.getElementType().getDisplayName(),
                List.of(place.getTerrainType()),
                description(place.getElementType()),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getMapUrl(),
                reviewCount,
                isSaved,
                isVisited
        );
    }

    private static String thumbnailUrl(Place place, String contextPathUrl) {
        if (!StringUtils.hasText(place.getGooglePlaceId())) {
            return null;
        }

        return UriComponentsBuilder.fromUriString(contextPathUrl)
                .path(THUMBNAIL_PATH)
                .build(place.getId())
                .toString();
    }

    private static PlaceDescription description(ElementType elementType) {
        return new PlaceDescription(DESCRIPTION_QUESTION, ELEMENT_DESCRIPTIONS.get(elementType));
    }

    public record PlaceDescription(String question, String answer) {
    }
}
