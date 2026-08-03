package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.entity.Place;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class PlaceThumbnailUrlFactory {

    public String create(Place place, String contextPathUrl) {
        if (!StringUtils.hasText(place.getGooglePlaceId())) {
            return null;
        }

        return UriComponentsBuilder.fromUriString(contextPathUrl)
                .path("/places/{placeId}/thumbnail")
                .build(place.getId())
                .toString();
    }
}
