package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.response.EditorPickItemResponse;
import com.umc.todayter.domain.place.dto.response.EditorPickResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchTypeResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EditorPickService {

    private final PlaceRepository placeRepository;
    private final PlaceThumbnailUrlFactory thumbnailUrlFactory;

    public EditorPickResponse getEditorPicks(Integer limit, String contextPathUrl) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(
                        Sort.Order.desc("averageRating"),
                        Sort.Order.asc("id")
                )
        );

        List<EditorPickItemResponse> content = placeRepository.findByActiveTrueAndEditorPickTrue(pageRequest).stream()
                .map(place -> toItemResponse(place, contextPathUrl))
                .toList();

        return new EditorPickResponse(content);
    }

    private EditorPickItemResponse toItemResponse(Place place, String contextPathUrl) {
        return new EditorPickItemResponse(
                place.getId(),
                place.getName(),
                thumbnailUrlFactory.create(place, contextPathUrl),
                place.getSummary(),
                place.getDescription(),
                new PlaceSearchTypeResponse(place.getElementType().name(), place.getElementType().getDisplayName()),
                new PlaceSearchTypeResponse(place.getThemeType().name(), place.getThemeType().getDisplayName()),
                place.getAverageRating()
        );
    }
}
