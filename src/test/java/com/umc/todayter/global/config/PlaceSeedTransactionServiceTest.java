package com.umc.todayter.global.config;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceSeedTransactionServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void seedPlace_insertsNewPlaceWhenNameDoesNotExist() {
        Place seedPlace = place("부산시민공원", "map-url", "google-place-id");
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.empty());
        when(placeRepository.saveAndFlush(seedPlace)).thenReturn(seedPlace);

        new PlaceSeedTransactionService(placeRepository).seedPlace(seedPlace);

        verify(placeRepository).saveAndFlush(seedPlace);
        verify(placeRepository, never()).flush();
    }

    @Test
    void seedPlace_updatesExistingMapUrlAndGooglePlaceIdWhenSeedValuesAreNotBlank() {
        Place seedPlace = place("부산시민공원", "new-map-url", "new-google-place-id");
        Place existingPlace = place("부산시민공원", "old-map-url", "old-google-place-id");
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.of(existingPlace));

        new PlaceSeedTransactionService(placeRepository).seedPlace(seedPlace);

        assertThat(existingPlace.getMapUrl()).isEqualTo("new-map-url");
        assertThat(existingPlace.getGooglePlaceId()).isEqualTo("new-google-place-id");
        verify(placeRepository).flush();
        verify(placeRepository, never()).saveAndFlush(any(Place.class));
    }

    @Test
    void seedPlace_doesNotOverwriteExistingValuesWhenSeedValuesAreBlank() {
        Place seedPlace = place("부산시민공원", null, null);
        Place existingPlace = place("부산시민공원", "existing-map-url", "existing-google-place-id");
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.of(existingPlace));

        new PlaceSeedTransactionService(placeRepository).seedPlace(seedPlace);

        assertThat(existingPlace.getMapUrl()).isEqualTo("existing-map-url");
        assertThat(existingPlace.getGooglePlaceId()).isEqualTo("existing-google-place-id");
        verify(placeRepository, never()).flush();
        verify(placeRepository, never()).saveAndFlush(any(Place.class));
    }

    @Test
    void seedPlace_doesNotCallInsertWriteMethodWhenSamePlaceIsProcessedAgain() {
        Place seedPlace = place("부산시민공원", "map-url", "google-place-id");
        Place existingPlace = place("부산시민공원", "map-url", "google-place-id");
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.empty());
        when(placeRepository.saveAndFlush(seedPlace)).thenReturn(seedPlace);

        PlaceSeedTransactionService service = new PlaceSeedTransactionService(placeRepository);
        service.seedPlace(seedPlace);

        clearInvocations(placeRepository);
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.of(existingPlace));

        service.seedPlace(seedPlace);

        verify(placeRepository, never()).saveAndFlush(any(Place.class));
    }

    @Test
    void seedPlace_wrapsInsertIntegrityViolationWithInsertOperation() {
        Place seedPlace = place("부산시민공원", "map-url", "google-place-id");
        DataIntegrityViolationException cause = new DataIntegrityViolationException("insert conflict");
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.empty());
        when(placeRepository.saveAndFlush(seedPlace)).thenThrow(cause);

        assertThatThrownBy(() -> new PlaceSeedTransactionService(placeRepository).seedPlace(seedPlace))
                .isInstanceOfSatisfying(PlaceSeedDataIntegrityException.class, e -> {
                    assertThat(e.getPlaceName()).isEqualTo("부산시민공원");
                    assertThat(e.getOperation()).isEqualTo(PlaceSeedDataIntegrityException.Operation.INSERT);
                    assertThat(e.getDataIntegrityViolationException()).isSameAs(cause);
                });
    }

    @Test
    void seedPlace_wrapsUpdateIntegrityViolationWithUpdateOperation() {
        Place seedPlace = place("부산시민공원", "new-map-url", "new-google-place-id");
        Place existingPlace = place("부산시민공원", "old-map-url", "old-google-place-id");
        DataIntegrityViolationException cause = new DataIntegrityViolationException("update conflict");
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.of(existingPlace));
        org.mockito.Mockito.doThrow(cause).when(placeRepository).flush();

        assertThatThrownBy(() -> new PlaceSeedTransactionService(placeRepository).seedPlace(seedPlace))
                .isInstanceOfSatisfying(PlaceSeedDataIntegrityException.class, e -> {
                    assertThat(e.getPlaceName()).isEqualTo("부산시민공원");
                    assertThat(e.getOperation()).isEqualTo(PlaceSeedDataIntegrityException.Operation.UPDATE);
                    assertThat(e.getDataIntegrityViolationException()).isSameAs(cause);
                });
    }

    @Test
    void existsByName_returnsWhetherPlaceExists() {
        when(placeRepository.findByName("부산시민공원")).thenReturn(Optional.of(place("부산시민공원", null, null)));

        boolean exists = new PlaceSeedTransactionService(placeRepository).existsByName("부산시민공원");

        assertThat(exists).isTrue();
    }

    private Place place(String name, String mapUrl, String googlePlaceId) {
        return Place.builder()
                .name(name)
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.BUSAN)
                .latitude(35.1684)
                .longitude(129.0562)
                .elementType(ElementType.WOOD)
                .themeType(ThemeType.HEALTH)
                .averageRating(0.0)
                .reviewCount(0)
                .editorPick(true)
                .active(true)
                .mapUrl(mapUrl)
                .googlePlaceId(googlePlaceId)
                .terrainType("park")
                .loveScore(22)
                .relationshipScore(26)
                .careerScore(18)
                .studyScore(24)
                .restScore(30)
                .transitionScore(26)
                .build();
    }
}
