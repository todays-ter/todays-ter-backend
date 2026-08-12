package com.umc.todayter.global.config;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceSeederTest {

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void run_savesAllSeedPlacesWhenDatabaseIsEmpty() throws IOException {
        when(placeRepository.count()).thenReturn(0L);

        new PlaceSeeder(placeRepository).run(null);

        ArgumentCaptor<List<Place>> placesCaptor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).saveAll(placesCaptor.capture());
        assertThat(placesCaptor.getValue()).hasSize(140);
        assertThat(placesCaptor.getValue())
                .extracting(Place::getRegionCode)
                .containsExactlyInAnyOrderElementsOf(regionCodes(60, 50, 30));
    }

    @Test
    void run_insertsMissingBusanAndJejuPlacesWhenOnlySeoulPlacesExist() throws IOException {
        List<Place> seedPlaces = seedPlaces();
        Map<String, Place> storedPlaces = new LinkedHashMap<>();
        seedPlaces.stream()
                .filter(place -> place.getRegionCode() == RegionCode.SEOUL)
                .forEach(place -> storedPlaces.put(place.getName(), copyForExistingPlace(place)));

        runSeederWithStoredPlaces(storedPlaces);

        assertThat(storedPlaces).hasSize(140);
        assertThat(storedPlaces.values())
                .filteredOn(place -> place.getRegionCode() == RegionCode.SEOUL)
                .hasSize(60);
        assertThat(storedPlaces.values())
                .filteredOn(place -> place.getRegionCode() == RegionCode.BUSAN)
                .hasSize(50);
        assertThat(storedPlaces.values())
                .filteredOn(place -> place.getRegionCode() == RegionCode.JEJU)
                .hasSize(30);
    }

    @Test
    void run_updatesExistingMapUrlAndGooglePlaceIdWhenSeedValuesAreNotBlank() throws IOException {
        Place seedPlace = seedPlaces().stream()
                .filter(place -> place.getRegionCode() == RegionCode.SEOUL)
                .findFirst()
                .orElseThrow();
        Place existingPlace = copyForExistingPlace(seedPlace);
        existingPlace.updateMapUrl("old-map-url");
        existingPlace.updateGooglePlaceId("old-google-place-id");
        Map<String, Place> storedPlaces = new LinkedHashMap<>();
        storedPlaces.put(existingPlace.getName(), existingPlace);

        runSeederWithStoredPlaces(storedPlaces);

        assertThat(existingPlace.getMapUrl()).isEqualTo(seedPlace.getMapUrl());
        assertThat(existingPlace.getGooglePlaceId()).isEqualTo(seedPlace.getGooglePlaceId());
    }

    @Test
    void run_doesNotOverwriteExistingMapUrlAndGooglePlaceIdWhenSeedValuesAreBlank() throws IOException {
        Place blankSeedPlace = Place.builder()
                .name("blank-place")
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.BUSAN)
                .latitude(35.1684)
                .longitude(129.0562)
                .elementType(com.umc.todayter.domain.place.enums.ElementType.WOOD)
                .themeType(com.umc.todayter.domain.place.enums.ThemeType.HEALTH)
                .averageRating(0.0)
                .reviewCount(0)
                .editorPick(true)
                .active(true)
                .terrainType("대형공원")
                .loveScore(22)
                .relationshipScore(26)
                .careerScore(18)
                .studyScore(24)
                .restScore(30)
                .transitionScore(26)
                .build();
        Place existingPlace = copyForExistingPlace(blankSeedPlace);
        existingPlace.updateMapUrl("existing-map-url");
        existingPlace.updateGooglePlaceId("existing-google-place-id");

        ReflectionTestUtils.invokeMethod(
                new PlaceSeeder(placeRepository),
                "syncExistingPlace",
                blankSeedPlace,
                existingPlace
        );

        assertThat(existingPlace.getMapUrl()).isEqualTo("existing-map-url");
        assertThat(existingPlace.getGooglePlaceId()).isEqualTo("existing-google-place-id");
    }

    @Test
    void run_doesNotCreateDuplicatePlacesWhenSeederRunsAgain() throws IOException {
        Map<String, Place> storedPlaces = new LinkedHashMap<>();

        runSeederWithStoredPlaces(storedPlaces);
        runSeederWithStoredPlaces(storedPlaces);

        assertThat(storedPlaces).hasSize(140);
    }

    private List<Place> seedPlaces() throws IOException {
        PlaceRepository repository = org.mockito.Mockito.mock(PlaceRepository.class);
        when(repository.count()).thenReturn(0L);

        new PlaceSeeder(repository).run(null);

        ArgumentCaptor<List<Place>> placesCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(placesCaptor.capture());
        return placesCaptor.getValue();
    }

    private void runSeederWithStoredPlaces(Map<String, Place> storedPlaces) throws IOException {
        when(placeRepository.count()).thenReturn((long) Math.max(storedPlaces.size(), 1));
        when(placeRepository.findByName(any(String.class)))
                .thenAnswer(invocation -> Optional.ofNullable(storedPlaces.get(invocation.getArgument(0))));
        lenient().when(placeRepository.save(any(Place.class)))
                .thenAnswer(invocation -> {
                    Place place = invocation.getArgument(0);
                    storedPlaces.putIfAbsent(place.getName(), place);
                    return place;
                });

        new PlaceSeeder(placeRepository).run(null);

        verify(placeRepository, atLeastOnce()).findByName(any(String.class));
        verify(placeRepository, never()).saveAll(any());
    }

    private Place copyForExistingPlace(Place seedPlace) {
        Place place = Place.builder()
                .name(seedPlace.getName())
                .summary(seedPlace.getSummary())
                .description(seedPlace.getDescription())
                .address(seedPlace.getAddress())
                .regionCode(seedPlace.getRegionCode())
                .latitude(seedPlace.getLatitude())
                .longitude(seedPlace.getLongitude())
                .elementType(seedPlace.getElementType())
                .themeType(seedPlace.getThemeType())
                .averageRating(seedPlace.getAverageRating())
                .reviewCount(seedPlace.getReviewCount())
                .editorPick(seedPlace.getEditorPick())
                .active(seedPlace.getActive())
                .terrainType(seedPlace.getTerrainType())
                .loveScore(seedPlace.getLoveScore())
                .relationshipScore(seedPlace.getRelationshipScore())
                .careerScore(seedPlace.getCareerScore())
                .studyScore(seedPlace.getStudyScore())
                .restScore(seedPlace.getRestScore())
                .transitionScore(seedPlace.getTransitionScore())
                .build();
        ReflectionTestUtils.setField(place, "mapUrl", seedPlace.getMapUrl());
        ReflectionTestUtils.setField(place, "googlePlaceId", seedPlace.getGooglePlaceId());
        return place;
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
