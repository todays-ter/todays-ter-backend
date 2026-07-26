package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.client.GooglePlacesClient;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceThumbnailServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private GooglePlacesClient googlePlacesClient;

    @InjectMocks
    private PlaceThumbnailService placeThumbnailService;

    @Test
    void getThumbnailUri_returnsHttpsUriWhenActivePlaceAndGoogleResponseExist() {
        Place place = placeWithGooglePlaceId("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id"))
                .thenReturn(Optional.of("places/google-place-id/photos/photo-resource"));
        when(googlePlacesClient.getPhotoUri("places/google-place-id/photos/photo-resource"))
                .thenReturn("https://lh3.googleusercontent.com/photo");

        URI result = placeThumbnailService.getThumbnailUri(1L);

        assertThat(result).isEqualTo(URI.create("https://lh3.googleusercontent.com/photo"));
    }

    @Test
    void getThumbnailUri_allowsValidHttpsUriWithHost() {
        Place place = placeWithGooglePlaceId("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id"))
                .thenReturn(Optional.of("places/google-place-id/photos/photo-resource"));
        when(googlePlacesClient.getPhotoUri("places/google-place-id/photos/photo-resource"))
                .thenReturn("https://lh3.googleusercontent.com/example.jpg");

        URI result = placeThumbnailService.getThumbnailUri(1L);

        assertThat(result).isEqualTo(URI.create("https://lh3.googleusercontent.com/example.jpg"));
    }

    @Test
    void getThumbnailUri_throwsNotFoundWhenPlaceDoesNotExist() {
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeThumbnailService.getThumbnailUri(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getThumbnailUri_throwsNotFoundWhenInactivePlaceIsNotReturned() {
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeThumbnailService.getThumbnailUri(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);

        verify(placeRepository).findByIdAndActiveTrue(1L);
    }

    @Test
    void getThumbnailUri_throwsNotFoundWhenGooglePlaceIdIsNull() {
        Place place = placeWithGooglePlaceId(null, true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));

        assertThatThrownBy(() -> placeThumbnailService.getThumbnailUri(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getThumbnailUri_throwsNotFoundWhenGooglePlaceIdIsBlank() {
        Place place = placeWithGooglePlaceId(" ", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));

        assertThatThrownBy(() -> placeThumbnailService.getThumbnailUri(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getThumbnailUri_throwsNotFoundWhenGooglePhotosDoNotExist() {
        Place place = placeWithGooglePlaceId("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeThumbnailService.getThumbnailUri(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getThumbnailUri_throwsInternalServerErrorWhenPhotoUriIsNull() {
        assertInvalidPhotoUri(null);
    }

    @Test
    void getThumbnailUri_throwsInternalServerErrorWhenPhotoUriIsBlank() {
        assertInvalidPhotoUri(" ");
    }

    @Test
    void getThumbnailUri_throwsInternalServerErrorWhenPhotoUriIsRelative() {
        assertInvalidPhotoUri("/relative/photo");
    }

    @Test
    void getThumbnailUri_throwsInternalServerErrorWhenPhotoUriIsHttp() {
        assertInvalidPhotoUri("http://lh3.googleusercontent.com/photo");
    }

    @Test
    void getThumbnailUri_throwsInternalServerErrorWhenHttpsUriHasNoHost() {
        assertInvalidPhotoUri("https:foo");
    }

    @Test
    void getThumbnailUri_throwsInternalServerErrorWhenHttpsUriHasUserInfo() {
        assertInvalidPhotoUri("https://user@example.com/image.jpg");
    }

    @Test
    void getThumbnailUri_propagatesGooglePlacesClientCommon500() {
        Place place = placeWithGooglePlaceId("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id"))
                .thenThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> placeThumbnailService.getThumbnailUri(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private void assertInvalidPhotoUri(String photoUri) {
        Place place = placeWithGooglePlaceId("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id"))
                .thenReturn(Optional.of("places/google-place-id/photos/photo-resource"));
        when(googlePlacesClient.getPhotoUri("places/google-place-id/photos/photo-resource"))
                .thenReturn(photoUri);

        assertThatThrownBy(() -> placeThumbnailService.getThumbnailUri(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private Place placeWithGooglePlaceId(String googlePlaceId, boolean active) {
        Place place = Place.builder()
                .name("name")
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5665)
                .longitude(126.9780)
                .elementType(ElementType.FIRE)
                .themeType(ThemeType.LOVE)
                .averageRating(0.0)
                .reviewCount(0)
                .editorPick(false)
                .active(active)
                .terrainType("기타")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(0)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
        ReflectionTestUtils.setField(place, "googlePlaceId", googlePlaceId);
        return place;
    }
}
