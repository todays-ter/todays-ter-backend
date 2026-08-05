package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.client.GooglePlacesClient;
import com.umc.todayter.domain.place.dto.response.PlaceShareCardResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.exception.PlaceErrorCode;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceShareCardService {

    private final PlaceRepository placeRepository;
    private final GooglePlacesClient googlePlacesClient;

    public PlaceShareCardResponse getShareCard(Long placeId) {
        Place place = placeRepository.findByIdAndActiveTrue(placeId)
                .orElseThrow(() -> new CustomException(PlaceErrorCode.PLACE_NOT_FOUND));

        String googlePlaceId = place.getGooglePlaceId();
        if (!StringUtils.hasText(googlePlaceId)) {
            throw new CustomException(PlaceErrorCode.PLACE_IMAGE_NOT_FOUND);
        }

        String photoName = googlePlacesClient.getFirstPhotoName(googlePlaceId)
                .orElseThrow(() -> new CustomException(PlaceErrorCode.PLACE_IMAGE_NOT_FOUND));

        String imageUrl = validatePhotoUri(googlePlacesClient.getPhotoUri(photoName));

        return new PlaceShareCardResponse(
                place.getName(),
                place.getElementType().getDisplayName(),
                imageUrl
        );
    }

    private String validatePhotoUri(String photoUri) {
        if (!StringUtils.hasText(photoUri)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        try {
            java.net.URI uri = java.net.URI.create(photoUri);
            if (!uri.isAbsolute()
                    || !"https".equalsIgnoreCase(uri.getScheme())
                    || !StringUtils.hasText(uri.getHost())
                    || uri.getUserInfo() != null) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            return photoUri;
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
