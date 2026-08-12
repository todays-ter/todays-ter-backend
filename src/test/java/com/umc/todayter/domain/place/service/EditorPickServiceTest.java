package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.response.EditorPickResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditorPickServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void getEditorPicks_usesFixedPageRequestAndSort() {
        EditorPickService editorPickService = service();
        when(placeRepository.findByActiveTrueAndEditorPickTrue(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(List.of());

        editorPickService.getEditorPicks(7, "http://localhost:8080");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(placeRepository).findByActiveTrueAndEditorPickTrue(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(7);
        assertThat(pageable.getSort().getOrderFor("averageRating").getDirection().isDescending()).isTrue();
        assertThat(pageable.getSort().getOrderFor("id").getDirection().isAscending()).isTrue();
    }

    @Test
    void getEditorPicks_returnsPlaceCardsWithAbsoluteThumbnailUrl() {
        EditorPickService editorPickService = service();
        Place place = place(1L, "Gyeongbokgung", ElementType.EARTH, ThemeType.WEALTH, 4.7, "google-place-id");
        when(placeRepository.findByActiveTrueAndEditorPickTrue(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(List.of(place));

        EditorPickResponse response = editorPickService.getEditorPicks(3, "http://localhost:8080");

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).placeId()).isEqualTo(1L);
        assertThat(response.content().get(0).placeName()).isEqualTo("Gyeongbokgung");
        assertThat(response.content().get(0).thumbnailUrl()).isEqualTo("http://localhost:8080/places/1/thumbnail");
        assertThat(response.content().get(0).summary()).isEqualTo("summary");
        assertThat(response.content().get(0).description()).isEqualTo("description");
        assertThat(response.content().get(0).element().code()).isEqualTo("EARTH");
        assertThat(response.content().get(0).element().name()).isEqualTo(ElementType.EARTH.getDisplayName());
        assertThat(response.content().get(0).theme().code()).isEqualTo("WEALTH");
        assertThat(response.content().get(0).theme().name()).isEqualTo(ThemeType.WEALTH.getDisplayName());
        assertThat(response.content().get(0).averageRating()).isEqualTo(4.7);
    }

    @Test
    void getEditorPicks_returnsNullThumbnailWhenGooglePlaceIdIsNullOrBlank() {
        EditorPickService editorPickService = service();
        Place nullGooglePlace = place(1L, "null-google", ElementType.FIRE, ThemeType.LOVE, 4.0, null);
        Place emptyGooglePlace = place(2L, "empty-google", ElementType.EARTH, ThemeType.WEALTH, 3.8, "");
        Place blankGooglePlace = place(3L, "blank-google", ElementType.WOOD, ThemeType.HEALTH, 3.5, " ");
        when(placeRepository.findByActiveTrueAndEditorPickTrue(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(List.of(nullGooglePlace, emptyGooglePlace, blankGooglePlace));

        EditorPickResponse response = editorPickService.getEditorPicks(3, "http://localhost:8080");

        assertThat(response.content())
                .extracting("thumbnailUrl")
                .containsExactly(null, null, null);
    }

    @Test
    void getEditorPicks_returnsEmptyContent() {
        EditorPickService editorPickService = service();
        when(placeRepository.findByActiveTrueAndEditorPickTrue(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(List.of());

        EditorPickResponse response = editorPickService.getEditorPicks(3, "http://localhost:8080");

        assertThat(response.content()).isEmpty();
    }

    private Place place(
            Long id,
            String name,
            ElementType elementType,
            ThemeType themeType,
            double averageRating,
            String googlePlaceId
    ) {
        Place place = Place.builder()
                .name(name)
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5796)
                .longitude(126.9770)
                .elementType(elementType)
                .themeType(themeType)
                .averageRating(averageRating)
                .reviewCount(0)
                .editorPick(true)
                .active(true)
                .googlePlaceId(googlePlaceId)
                .terrainType("terrain")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(0)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }

    private EditorPickService service() {
        return new EditorPickService(placeRepository, new PlaceThumbnailUrlFactory());
    }
}
