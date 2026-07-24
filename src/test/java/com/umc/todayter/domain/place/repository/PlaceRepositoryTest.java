package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PlaceRepositoryTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Test
    void countActivePlacesGroupByThemeType_countsOnlyActivePlacesAndReturnsExistingThemes() {
        placeRepository.save(place("love-active-1", ThemeType.LOVE, true));
        placeRepository.save(place("love-active-2", ThemeType.LOVE, true));
        placeRepository.save(place("love-inactive", ThemeType.LOVE, false));
        placeRepository.save(place("career-active", ThemeType.CAREER, true));
        placeRepository.flush();

        Map<ThemeType, Long> counts = placeRepository.countActivePlacesGroupByThemeType().stream()
                .collect(Collectors.toMap(
                        ThemePlaceCount::getThemeType,
                        ThemePlaceCount::getPlaceCount,
                        Long::sum,
                        () -> new EnumMap<>(ThemeType.class)
                ));

        assertThat(counts).containsEntry(ThemeType.LOVE, 2L);
        assertThat(counts).containsEntry(ThemeType.CAREER, 1L);
        assertThat(counts).containsOnlyKeys(ThemeType.LOVE, ThemeType.CAREER);
        assertThat(counts).doesNotContainKey(ThemeType.HEALTH);
    }

    private Place place(String name, ThemeType themeType, boolean active) {
        return Place.create(
                name,
                "summary",
                "description",
                "address",
                RegionCode.SEOUL,
                37.5665,
                126.9780,
                ElementType.FIRE,
                themeType,
                0.0,
                0,
                false,
                active
        );
    }
}
