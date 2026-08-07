package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.client.GooglePlacesClient;
import com.umc.todayter.domain.place.dto.response.PlaceShareCardResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.exception.PlaceErrorCode;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceShareCardServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private GooglePlacesClient googlePlacesClient;

    @InjectMocks
    private PlaceShareCardService placeShareCardService;

    @Test
    void getShareCard_returnsPlaceNameElementAndGoogleImageUrl() {
        Place place = place("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id"))
                .thenReturn(Optional.of("places/google-place-id/photos/photo-resource"));
        when(googlePlacesClient.getPhotoUri("places/google-place-id/photos/photo-resource"))
                .thenReturn("https://lh3.googleusercontent.com/photo");

        PlaceShareCardResponse response = placeShareCardService.getShareCard(1L);

        assertThat(response.placeName()).isEqualTo("청계천 모전교");
        assertThat(response.element()).isEqualTo("수");
        assertThat(response.imageUrl()).isEqualTo("https://lh3.googleusercontent.com/photo");
    }

    @Test
    void getShareCard_placeNotFound_throwsPlaceNotFound() {
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeShareCardService.getShareCard(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.PLACE_NOT_FOUND);
    }

    @Test
    void getShareCard_noGooglePlaceId_throwsPlaceImageNotFound() {
        Place place = place(null, true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));

        assertThatThrownBy(() -> placeShareCardService.getShareCard(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.PLACE_IMAGE_NOT_FOUND);
    }

    @Test
    void getShareCard_noGooglePhoto_throwsPlaceImageNotFound() {
        Place place = place("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeShareCardService.getShareCard(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.PLACE_IMAGE_NOT_FOUND);
    }

    @Test
    void getShareCard_invalidPhotoUri_throwsInternalServerError() {
        Place place = place("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id"))
                .thenReturn(Optional.of("places/google-place-id/photos/photo-resource"));
        when(googlePlacesClient.getPhotoUri("places/google-place-id/photos/photo-resource"))
                .thenReturn("http://insecure.example.com/photo");

        assertThatThrownBy(() -> placeShareCardService.getShareCard(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getShareCard_propagatesGooglePlacesClientCommon500() {
        Place place = place("google-place-id", true);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(googlePlacesClient.getFirstPhotoName("google-place-id"))
                .thenThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> placeShareCardService.getShareCard(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private Place place(String googlePlaceId, boolean active) {
        Place place = Place.builder()
                .name("청계천 모전교")
                .summary("summary")
                .description("description")
                .address("서울 중구 무교동")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5665)
                .longitude(126.9780)
                .elementType(ElementType.WATER)
                .themeType(ThemeType.HEALTH)
                .averageRating(0.0)
                .reviewCount(0)
                .editorPick(false)
                .active(active)
                .terrainType("하천")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(0)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
        ReflectionTestUtils.setField(place, "id", 1L);
        ReflectionTestUtils.setField(place, "googlePlaceId", googlePlaceId);
        return place;
    }
}
