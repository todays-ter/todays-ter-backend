package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.place.dto.internal.PlaceRecommendationAiContent;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRecommendationSnapshotRepository;
import com.umc.todayter.domain.place.service.provider.PlaceRecommendationAiContentParser;
import com.umc.todayter.domain.place.service.provider.PlaceRecommendationPromptProvider;
import com.umc.todayter.global.config.client.OpenAiFortuneReportClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceRecommendationSnapshotServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-03T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private PlaceRecommendationSnapshotRepository snapshotRepository;

    @Mock
    private RecommendationMatchingService matchingService;

    @Mock
    private PlaceRecommendationPromptProvider promptProvider;

    @Mock
    private PlaceRecommendationAiContentParser contentParser;

    @Mock
    private OpenAiFortuneReportClient openAiClient;

    @Test
    void getOrCreateWithMatchReturnsCachedSnapshotWithoutOpenAi() {
        Place place = place(1L);
        RecommendationMatchContext match = match(99L);
        PlaceRecommendationSnapshot cached = PlaceRecommendationSnapshot.create(
                99L,
                1L,
                LocalDate.of(2026, 8, 3),
                "NONE",
                80,
                List.of("point"),
                "cached reason",
                "action"
        );
        when(snapshotRepository.findByFortuneReportIdAndPlaceIdAndRecommendationDateAndConcernKey(
                99L, 1L, LocalDate.of(2026, 8, 3), "NONE"
        )).thenReturn(Optional.of(cached));

        PlaceRecommendationSnapshot result = service().getOrCreate(match, place);

        assertThat(result).isEqualTo(cached);
        verify(openAiClient, never()).generate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getOrCreateWithMatchCreatesSnapshotUsingProvidedScoreAndReportId() {
        Place place = place(1L);
        RecommendationMatchContext match = match(99L);
        when(snapshotRepository.findByFortuneReportIdAndPlaceIdAndRecommendationDateAndConcernKey(
                99L, 1L, LocalDate.of(2026, 8, 3), "NONE"
        )).thenReturn(Optional.empty());
        when(promptProvider.create(match, place)).thenReturn("prompt");
        when(openAiClient.generate("prompt")).thenReturn("ai-output");
        when(contentParser.parse("ai-output")).thenReturn(new PlaceRecommendationAiContent("reason", "action"));
        when(snapshotRepository.save(org.mockito.ArgumentMatchers.any(PlaceRecommendationSnapshot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlaceRecommendationSnapshot result = service().getOrCreate(match, place);

        assertThat(result.getFortuneReportId()).isEqualTo(99L);
        assertThat(result.getPlaceId()).isEqualTo(1L);
        assertThat(result.getMatchingScore()).isEqualTo(80);
        assertThat(result.getWhyItMatches()).isEqualTo("reason");
        verify(openAiClient).generate("prompt");
    }

    private PlaceRecommendationSnapshotService service() {
        return new PlaceRecommendationSnapshotService(
                snapshotRepository,
                matchingService,
                promptProvider,
                contentParser,
                openAiClient,
                FIXED_CLOCK
        );
    }

    private RecommendationMatchContext match(Long reportId) {
        BasicReport basicReport = org.mockito.Mockito.mock(BasicReport.class);
        return new RecommendationMatchContext(
                reportId,
                basicReport,
                ElementType.FIRE,
                ElementType.WOOD,
                List.of(),
                50,
                20,
                10
        );
    }

    private Place place(Long id) {
        Place place = Place.builder()
                .name("place")
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.0)
                .longitude(127.0)
                .elementType(ElementType.FIRE)
                .themeType(ThemeType.LOVE)
                .averageRating(4.0)
                .reviewCount(0)
                .editorPick(false)
                .active(true)
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
}
