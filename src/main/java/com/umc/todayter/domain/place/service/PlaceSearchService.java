package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.request.PlaceSearchRequest;
import com.umc.todayter.domain.place.dto.response.PlaceSearchAppliedFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchPageResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchTypeResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.PlaceSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceSearchService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final PlaceRepository placeRepository;

    public PlaceSearchResponse searchPlaces(PlaceSearchRequest request, String contextPathUrl) {
        String keyword = request.getTrimmedKeyword();
        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(
                        Sort.Order.desc("averageRating"),
                        Sort.Order.asc("id")
                )
        );

        Specification<Place> specification = PlaceSpecifications.active()
                .and(PlaceSpecifications.keywordContains(keyword))
                .and(PlaceSpecifications.regionCodeEquals(request.getRegionCode()))
                .and(PlaceSpecifications.themeTypeEquals(request.getThemeType()))
                .and(PlaceSpecifications.elementTypeEquals(request.getElementType()));

        Page<Place> places = placeRepository.findAll(specification, pageRequest);
        List<PlaceSearchItemResponse> content = places.stream()
                .map(place -> toItemResponse(place, request, contextPathUrl))
                .toList();

        return new PlaceSearchResponse(
                PlaceSearchAppliedFiltersResponse.of(
                        keyword,
                        request.getRegionCode(),
                        request.getThemeType(),
                        request.getElementType()
                ),
                content,
                PlaceSearchPageResponse.from(places)
        );
    }

    private PlaceSearchItemResponse toItemResponse(Place place, PlaceSearchRequest request, String contextPathUrl) {
        return new PlaceSearchItemResponse(
                place.getId(),
                place.getName(),
                thumbnailUrl(place, contextPathUrl),
                place.getSummary(),
                new PlaceSearchTypeResponse(place.getElementType().name(), place.getElementType().getDisplayName()),
                new PlaceSearchTypeResponse(place.getThemeType().name(), place.getThemeType().getDisplayName()),
                place.getAverageRating(),
                distanceKm(request, place)
        );
    }

    private String thumbnailUrl(Place place, String contextPathUrl) {
        if (!StringUtils.hasText(place.getGooglePlaceId())) {
            return null;
        }

        return UriComponentsBuilder.fromUriString(contextPathUrl)
                .path("/places/{placeId}/thumbnail")
                .build(place.getId())
                .toString();
    }

    private Double distanceKm(PlaceSearchRequest request, Place place) {
        if (!request.hasCoordinates()) {
            return null;
        }

        double latitudeDistance = Math.toRadians(place.getLatitude() - request.getLatitude());
        double longitudeDistance = Math.toRadians(place.getLongitude() - request.getLongitude());
        double requestLatitude = Math.toRadians(request.getLatitude());
        double placeLatitude = Math.toRadians(place.getLatitude());

        double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(requestLatitude) * Math.cos(placeLatitude)
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(EARTH_RADIUS_KM * c * 10.0) / 10.0;
    }
}
