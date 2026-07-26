package com.umc.todayter.domain.place.client;

import com.umc.todayter.domain.place.client.dto.GooglePhotoMediaResponse;
import com.umc.todayter.domain.place.client.dto.GooglePlaceDetailsResponse;
import com.umc.todayter.domain.place.config.GooglePlacesProperties;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.util.Arrays;
import java.util.Optional;

@Component
public class GooglePlacesClient {

    private static final String FIELD_MASK_PHOTOS = "photos";

    private final RestClient restClient;
    private final GooglePlacesProperties properties;

    public GooglePlacesClient(
            @Qualifier("googlePlacesRestClient") RestClient restClient,
            GooglePlacesProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public Optional<String> getFirstPhotoName(String googlePlaceId) {
        validateApiKey();

        try {
            GooglePlaceDetailsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("places", googlePlaceId)
                            .build())
                    .header("X-Goog-Api-Key", properties.apiKey())
                    .header("X-Goog-FieldMask", FIELD_MASK_PHOTOS)
                    .retrieve()
                    .body(GooglePlaceDetailsResponse.class);

            return Optional.ofNullable(response)
                    .map(GooglePlaceDetailsResponse::photos)
                    .filter(photos -> !photos.isEmpty())
                    .map(photos -> photos.get(0))
                    .map(GooglePlaceDetailsResponse.Photo::name)
                    .filter(StringUtils::hasText);
        } catch (RestClientResponseException | ResourceAccessException | HttpMessageConversionException e) {
            throw googlePlacesFailure();
        } catch (RestClientException e) {
            throw googlePlacesFailure();
        }
    }

    public String getPhotoUri(String photoName) {
        validateApiKey();

        if (!StringUtils.hasText(photoName)) {
            throw googlePlacesFailure();
        }

        String[] photoNameSegments = Arrays.stream(photoName.split("/"))
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        try {
            GooglePhotoMediaResponse response = restClient.get()
                    .uri(uriBuilder -> buildPhotoMediaUri(uriBuilder, photoNameSegments))
                    .retrieve()
                    .body(GooglePhotoMediaResponse.class);

            if (response == null || !StringUtils.hasText(response.photoUri())) {
                throw googlePlacesFailure();
            }

            return response.photoUri();
        } catch (RestClientResponseException | ResourceAccessException | HttpMessageConversionException e) {
            throw googlePlacesFailure();
        } catch (RestClientException e) {
            throw googlePlacesFailure();
        }
    }

    private java.net.URI buildPhotoMediaUri(UriBuilder uriBuilder, String[] photoNameSegments) {
        UriBuilder pathBuilder = uriBuilder;
        for (String segment : photoNameSegments) {
            pathBuilder = pathBuilder.pathSegment(segment);
        }

        return pathBuilder
                .pathSegment("media")
                .queryParam("maxWidthPx", properties.thumbnailMaxWidth())
                .queryParam("skipHttpRedirect", true)
                .queryParam("key", properties.apiKey())
                .build();
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw googlePlacesFailure();
        }
    }

    private CustomException googlePlacesFailure() {
        return new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
