package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.client.GooglePlacesClient;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class PlaceThumbnailService {

    private final PlaceRepository placeRepository;
    private final GooglePlacesClient googlePlacesClient;

    public URI getThumbnailUri(Long placeId) {
        Place place = placeRepository.findByIdAndActiveTrue(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        String googlePlaceId = place.getGooglePlaceId();
        if (!StringUtils.hasText(googlePlaceId)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        String photoName = googlePlacesClient.getFirstPhotoName(googlePlaceId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return validatePhotoUri(googlePlacesClient.getPhotoUri(photoName));
    }

    private URI validatePhotoUri(String photoUri) {
        if (!StringUtils.hasText(photoUri)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        try {
            URI uri = URI.create(photoUri);
            if (!uri.isAbsolute()
                    || !"https".equalsIgnoreCase(uri.getScheme())
                    || !StringUtils.hasText(uri.getHost())
                    || uri.getUserInfo() != null) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
