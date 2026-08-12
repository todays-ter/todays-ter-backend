package com.umc.todayter.global.config;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.RegionCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceSeederTest {

    @Mock
    private PlaceSeedTransactionService placeSeedTransactionService;

    @Test
    void run_delegatesAllSeedPlacesAndValidatesSeedDataContract() throws IOException {
        new PlaceSeeder(placeSeedTransactionService).run(null);

        ArgumentCaptor<Place> placeCaptor = ArgumentCaptor.forClass(Place.class);
        verify(placeSeedTransactionService, times(140)).seedPlace(placeCaptor.capture());

        List<Place> seedPlaces = placeCaptor.getAllValues();
        assertThat(seedPlaces).hasSize(140);
        assertThat(seedPlaces)
                .extracting(Place::getRegionCode)
                .containsExactlyInAnyOrderElementsOf(regionCodes(60, 50, 30));

        assertThat(seedPlaces)
                .extracting(Place::getGooglePlaceId)
                .allSatisfy(googlePlaceId -> assertThat(googlePlaceId).isNotBlank());
        Set<String> googlePlaceIds = seedPlaces.stream()
                .map(Place::getGooglePlaceId)
                .collect(Collectors.toSet());
        assertThat(googlePlaceIds).hasSize(140);

        assertThat(seedPlaces)
                .filteredOn(place -> place.getRegionCode() == RegionCode.SEOUL)
                .extracting(Place::getMapUrl)
                .allSatisfy(mapUrl -> assertThat(mapUrl).isNotBlank());
    }

    @Test
    void run_ignoresInsertFailureWhenFailedPlaceNameExistsAfterRollback() throws IOException {
        DataIntegrityViolationException cause = new DataIntegrityViolationException("name race");
        PlaceSeedDataIntegrityException exception = new PlaceSeedDataIntegrityException(
                "부산시민공원",
                PlaceSeedDataIntegrityException.Operation.INSERT,
                cause
        );
        doThrow(exception)
                .doNothing()
                .when(placeSeedTransactionService)
                .seedPlace(any(Place.class));
        when(placeSeedTransactionService.existsByName(anyString())).thenReturn(true);

        new PlaceSeeder(placeSeedTransactionService).run(null);

        verify(placeSeedTransactionService, times(140)).seedPlace(any(Place.class));
        verify(placeSeedTransactionService).existsByName(anyString());
    }

    @Test
    void run_propagatesInsertFailureWhenFailedPlaceNameDoesNotExistAfterRollback() {
        DataIntegrityViolationException cause = new DataIntegrityViolationException("googlePlaceId conflict");
        PlaceSeedDataIntegrityException exception = new PlaceSeedDataIntegrityException(
                "부산시민공원",
                PlaceSeedDataIntegrityException.Operation.INSERT,
                cause
        );
        doThrow(exception)
                .when(placeSeedTransactionService)
                .seedPlace(any(Place.class));
        when(placeSeedTransactionService.existsByName(anyString())).thenReturn(false);

        assertThatThrownBy(() -> new PlaceSeeder(placeSeedTransactionService).run(null))
                .isSameAs(cause);
    }

    @Test
    void run_propagatesExistingPlaceUpdateIntegrityFailure() {
        DataIntegrityViolationException cause = new DataIntegrityViolationException("update conflict");
        PlaceSeedDataIntegrityException exception = new PlaceSeedDataIntegrityException(
                "부산시민공원",
                PlaceSeedDataIntegrityException.Operation.UPDATE,
                cause
        );
        doThrow(exception)
                .when(placeSeedTransactionService)
                .seedPlace(any(Place.class));

        assertThatThrownBy(() -> new PlaceSeeder(placeSeedTransactionService).run(null))
                .isSameAs(cause);
    }

    private List<RegionCode> regionCodes(int seoulCount, int busanCount, int jejuCount) {
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.concat(
                                java.util.stream.Stream.generate(() -> RegionCode.SEOUL).limit(seoulCount),
                                java.util.stream.Stream.generate(() -> RegionCode.BUSAN).limit(busanCount)
                        ),
                        java.util.stream.Stream.generate(() -> RegionCode.JEJU).limit(jejuCount)
                )
                .toList();
    }
}
