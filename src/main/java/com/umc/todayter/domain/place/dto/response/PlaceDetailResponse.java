package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;

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
        Long reviewCount,
        Boolean isSaved,
        Boolean isVisited
) {
    // 오행별 고정 설명 텍스트 (place별 데이터가 아직 없어 오행 5종에 대한 템플릿으로 대체)
    private static final Map<ElementType, PlaceDescription> DESCRIPTIONS_BY_ELEMENT = Map.of(
            ElementType.FIRE, new PlaceDescription(
                    "이 터의 특징은 무엇인가요?",
                    "화(火) 기운이 강해 열정과 추진력을 북돋우고 새로운 시작에 좋은 기운을 더해줘요."
            ),
            ElementType.EARTH, new PlaceDescription(
                    "이 터의 특징은 무엇인가요?",
                    "토(土) 기운이 강해 안정감과 중심을 잡아주고 마음을 편안하게 다잡기에 좋아요."
            ),
            ElementType.WOOD, new PlaceDescription(
                    "이 터의 특징은 무엇인가요?",
                    "목(木) 기운이 강해 성장과 확장의 에너지를 주고 새로운 관계를 시작하기에 좋아요."
            ),
            ElementType.WATER, new PlaceDescription(
                    "이 터의 특징은 무엇인가요?",
                    "수(水) 기운이 강해 감정 정리와 회복에 좋고 오늘의 흐름과 잘 맞아요."
            ),
            ElementType.METAL, new PlaceDescription(
                    "이 터의 특징은 무엇인가요?",
                    "금(金) 기운이 강해 결단력과 정리의 힘을 주고 마무리가 필요한 일에 좋아요."
            )
    );

    public static PlaceDetailResponse from(
            Place place,
            Long reviewCount,
            boolean isSaved,
            boolean isVisited
    ) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                "/places/%d/thumbnail".formatted(place.getId()),
                place.getElementType().getDisplayName(),
                List.of(place.getTerrainType()),
                DESCRIPTIONS_BY_ELEMENT.get(place.getElementType()),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                reviewCount,
                isSaved,
                isVisited
        );
    }

    public record PlaceDescription(String question, String answer) {
    }
}
