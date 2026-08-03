package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void findByIdAndActiveTrue_returnsOnlyActivePlace() {
        Place activePlace = place("active", ThemeType.LOVE, true);
        Place inactivePlace = place("inactive", ThemeType.LOVE, false);
        placeRepository.save(activePlace);
        placeRepository.save(inactivePlace);
        placeRepository.flush();

        assertThat(placeRepository.findByIdAndActiveTrue(activePlace.getId())).contains(activePlace);
        assertThat(placeRepository.findByIdAndActiveTrue(inactivePlace.getId())).isEmpty();
    }

    @Test
    void findByActiveTrueAndEditorPickTrue_returnsOnlyActiveEditorPicks() {
        placeRepository.save(place("editor-active", ThemeType.LOVE, true, true, 4.0));
        placeRepository.save(place("editor-inactive", ThemeType.LOVE, false, true, 5.0));
        placeRepository.save(place("not-editor-active", ThemeType.LOVE, true, false, 5.0));
        placeRepository.save(place("not-editor-inactive", ThemeType.LOVE, false, false, 5.0));
        placeRepository.flush();

        List<Place> result = placeRepository.findByActiveTrueAndEditorPickTrue(PageRequest.of(0, 10, fixedSort()));

        assertThat(result).extracting(Place::getName).containsExactly("editor-active");
    }

    @Test
    void findByActiveTrueAndEditorPickTrue_sortsByAverageRatingDescAndIdAsc() {
        Place low = placeRepository.save(place("editor-sort-low", ThemeType.LOVE, true, true, 3.0));
        Place highFirst = placeRepository.save(place("editor-sort-high-first", ThemeType.LOVE, true, true, 5.0));
        Place highSecond = placeRepository.save(place("editor-sort-high-second", ThemeType.LOVE, true, true, 5.0));
        placeRepository.flush();

        List<Place> result = placeRepository.findByActiveTrueAndEditorPickTrue(PageRequest.of(0, 10, fixedSort()));

        assertThat(highFirst.getId()).isLessThan(highSecond.getId());
        assertThat(result)
                .extracting(Place::getId)
                .containsExactly(highFirst.getId(), highSecond.getId(), low.getId());
    }

    @Test
    void findByActiveTrueAndEditorPickTrue_limitsResults() {
        placeRepository.save(place("editor-limit-a", ThemeType.LOVE, true, true, 4.9));
        placeRepository.save(place("editor-limit-b", ThemeType.LOVE, true, true, 4.7));
        placeRepository.save(place("editor-limit-c", ThemeType.LOVE, true, true, 4.5));
        placeRepository.save(place("editor-limit-d", ThemeType.LOVE, true, true, 4.0));
        placeRepository.flush();

        List<Place> oneResult = placeRepository.findByActiveTrueAndEditorPickTrue(PageRequest.of(0, 1, fixedSort()));
        List<Place> threeResults = placeRepository.findByActiveTrueAndEditorPickTrue(PageRequest.of(0, 3, fixedSort()));

        assertThat(oneResult).hasSize(1);
        assertThat(oneResult).extracting(Place::getName).containsExactly("editor-limit-a");
        assertThat(threeResults).hasSize(3);
        assertThat(threeResults)
                .extracting(Place::getName)
                .containsExactly("editor-limit-a", "editor-limit-b", "editor-limit-c")
                .doesNotContain("editor-limit-d");
    }

    @Test
    void findByActiveTrueAndEditorPickTrue_returnsEmptyListWhenNoResult() {
        placeRepository.save(place("editor-empty-inactive", ThemeType.LOVE, false, true, 5.0));
        placeRepository.save(place("editor-empty-not-pick", ThemeType.LOVE, true, false, 5.0));
        placeRepository.flush();

        List<Place> result = placeRepository.findByActiveTrueAndEditorPickTrue(PageRequest.of(0, 3, fixedSort()));

        assertThat(result).isEmpty();
    }

    @Test
    void googlePlaceId_allowsMultipleNullValues() {
        placeRepository.save(place("null-google-place-id-1", ThemeType.LOVE, true));
        placeRepository.save(place("null-google-place-id-2", ThemeType.CAREER, true));

        placeRepository.flush();

        assertThat(placeRepository.count()).isEqualTo(2);
    }

    @Test
    void googlePlaceId_rejectsDuplicateNonNullValues() {
        Place first = place("google-place-id-1", ThemeType.LOVE, true, "duplicate-google-place-id");
        Place second = place("google-place-id-2", ThemeType.CAREER, true, "duplicate-google-place-id");

        assertThatThrownBy(() -> {
            placeRepository.save(first);
            placeRepository.save(second);
            placeRepository.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void searchPlaces_withoutFilters_returnsOnlyActivePlaces() {
        placeRepository.save(place("search-active", ThemeType.LOVE, true));
        placeRepository.save(place("search-inactive", ThemeType.LOVE, false));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec(null, null, null, null), pageRequest());

        assertThat(result.getContent()).extracting(Place::getName).contains("search-active");
        assertThat(result.getContent()).extracting(Place::getName).doesNotContain("search-inactive");
    }

    @Test
    void searchPlaces_keywordMatchesNameIgnoringCase() {
        placeRepository.save(place("Royal Palace", "quiet", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("no-match-name", "quiet", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec("palace", null, null, null), pageRequest());

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("Royal Palace");
    }

    @Test
    void searchPlaces_keywordMatchesSummaryIgnoringCase() {
        placeRepository.save(place("summary-match", "PALACE mood", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("no-match-summary", "quiet", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec("palace", null, null, null), pageRequest());

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("summary-match");
    }

    @Test
    void searchPlaces_keywordMatchesAddressIgnoringCase() {
        placeRepository.save(place("address-match", "quiet", "description", "old palace road", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("no-match-address", "quiet", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec("palace", null, null, null), pageRequest());

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("address-match");
    }

    @Test
    void searchPlaces_keywordTreatsPercentAsLiteral() {
        placeRepository.save(place("percent-match", "save 20% today", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("percent-normal", "save 20 percent today", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec("%", null, null, null), pageRequest());

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("percent-match");
    }

    @Test
    void searchPlaces_keywordTreatsUnderscoreAsLiteral() {
        placeRepository.save(place("underscore_match", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("underscore-normal", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec("_", null, null, null), pageRequest());

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("underscore_match");
    }

    @Test
    void searchPlaces_keywordTreatsBackslashAsLiteral() {
        placeRepository.save(place("backslash-match", "summary", "description", "path\\to\\place", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("backslash-normal", "summary", "description", "path/to/place", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec("\\", null, null, null), pageRequest());

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("backslash-match");
    }

    @Test
    void searchPlaces_filtersByRegionThemeAndElement() {
        placeRepository.save(place("region-match", "summary", "description", "address", RegionCode.JEJU, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("theme-match", "summary", "description", "address", RegionCode.SEOUL, ThemeType.WEALTH, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("element-match", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.EARTH, true, 4.0));
        placeRepository.flush();

        assertThat(placeRepository.findAll(searchSpec(null, RegionCode.JEJU, null, null), pageRequest()).getContent())
                .extracting(Place::getName)
                .containsExactly("region-match");
        assertThat(placeRepository.findAll(searchSpec(null, null, ThemeType.WEALTH, null), pageRequest()).getContent())
                .extracting(Place::getName)
                .containsExactly("theme-match");
        assertThat(placeRepository.findAll(searchSpec(null, null, null, ElementType.EARTH), pageRequest()).getContent())
                .extracting(Place::getName)
                .containsExactly("element-match");
    }

    @Test
    void searchPlaces_combinesConditionsWithAnd() {
        placeRepository.save(place("and-match-palace", "summary", "description", "address", RegionCode.SEOUL, ThemeType.WEALTH, ElementType.EARTH, true, 4.0));
        placeRepository.save(place("and-other-region-palace", "summary", "description", "address", RegionCode.JEJU, ThemeType.WEALTH, ElementType.EARTH, true, 4.0));
        placeRepository.save(place("and-other-theme-palace", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.EARTH, true, 4.0));
        placeRepository.save(place("and-other-element-palace", "summary", "description", "address", RegionCode.SEOUL, ThemeType.WEALTH, ElementType.FIRE, true, 4.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(
                searchSpec("palace", RegionCode.SEOUL, ThemeType.WEALTH, ElementType.EARTH),
                pageRequest()
        );

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("and-match-palace");
    }

    @Test
    void searchPlaces_sortsByAverageRatingDescAndIdAsc() {
        Place low = placeRepository.save(place("sort-low", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 3.0));
        Place highFirst = placeRepository.save(place("sort-high-first", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 5.0));
        Place highSecond = placeRepository.save(place("sort-high-second", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 5.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec(null, null, null, null), pageRequest());

        assertThat(result.getContent())
                .extracting(Place::getId)
                .containsSubsequence(highFirst.getId(), highSecond.getId(), low.getId());
    }

    @Test
    void searchPlaces_paginatesResults() {
        placeRepository.save(place("page-first", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 5.0));
        placeRepository.save(place("page-second", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 4.0));
        placeRepository.save(place("page-third", "summary", "description", "address", RegionCode.SEOUL, ThemeType.LOVE, ElementType.FIRE, true, 3.0));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(
                searchSpec(null, null, null, null),
                PageRequest.of(1, 1, fixedSort())
        );

        assertThat(result.getContent()).extracting(Place::getName).containsExactly("page-second");
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void searchPlaces_returnsEmptyPageWhenNoResult() {
        placeRepository.save(place("empty-source", ThemeType.LOVE, true));
        placeRepository.flush();

        Page<Place> result = placeRepository.findAll(searchSpec("missing", null, null, null), pageRequest());

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private Place place(String name, ThemeType themeType, boolean active) {
        return place(name, themeType, active, null);
    }

    private Place place(String name, ThemeType themeType, boolean active, String googlePlaceId) {
        return place(name, "summary", "description", "address", RegionCode.SEOUL, themeType, ElementType.FIRE, active, 0.0, googlePlaceId);
    }

    private Place place(String name, ThemeType themeType, boolean active, boolean editorPick, double averageRating) {
        return place(name, "summary", "description", "address", RegionCode.SEOUL, themeType, ElementType.FIRE, active, editorPick, averageRating, null);
    }

    private Place place(
            String name,
            String summary,
            String description,
            String address,
            RegionCode regionCode,
            ThemeType themeType,
            ElementType elementType,
            boolean active,
            double averageRating
    ) {
        return place(name, summary, description, address, regionCode, themeType, elementType, active, averageRating, null);
    }

    private Place place(
            String name,
            String summary,
            String description,
            String address,
            RegionCode regionCode,
            ThemeType themeType,
            ElementType elementType,
            boolean active,
            double averageRating,
            String googlePlaceId
    ) {
        return place(name, summary, description, address, regionCode, themeType, elementType, active, false, averageRating, googlePlaceId);
    }

    private Place place(
            String name,
            String summary,
            String description,
            String address,
            RegionCode regionCode,
            ThemeType themeType,
            ElementType elementType,
            boolean active,
            boolean editorPick,
            double averageRating,
            String googlePlaceId
    ) {
        return Place.builder()
                .name(name)
                .summary(summary)
                .description(description)
                .address(address)
                .regionCode(regionCode)
                .latitude(37.5665)
                .longitude(126.9780)
                .elementType(elementType)
                .themeType(themeType)
                .averageRating(averageRating)
                .reviewCount(0)
                .editorPick(editorPick)
                .active(active)
                .googlePlaceId(googlePlaceId)
                .terrainType("terrain")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(0)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
    }

    private Specification<Place> searchSpec(
            String keyword,
            RegionCode regionCode,
            ThemeType themeType,
            ElementType elementType
    ) {
        return PlaceSpecifications.active()
                .and(PlaceSpecifications.keywordContains(keyword))
                .and(PlaceSpecifications.regionCodeEquals(regionCode))
                .and(PlaceSpecifications.themeTypeEquals(themeType))
                .and(PlaceSpecifications.elementTypeEquals(elementType));
    }

    private PageRequest pageRequest() {
        return PageRequest.of(0, 20, fixedSort());
    }

    private Sort fixedSort() {
        return Sort.by(
                Sort.Order.desc("averageRating"),
                Sort.Order.asc("id")
        );
    }
}
