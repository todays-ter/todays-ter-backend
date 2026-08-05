package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.ConcernType;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.dto.internal.RecommendationScoringContext;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.service.provider.DailyFortuneElementProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationMatchingServiceTest {

    @Mock
    private FortuneReportRepository fortuneReportRepository;

    @Mock
    private OnboardingRepository onboardingRepository;

    @Mock
    private FortuneReportResultParser resultParser;

    @Mock
    private DailyFortuneElementProvider dailyElementProvider;

    @Test
    void prepareBuildsReusableScoringContextOnce() {
        FortuneReport report = report(10L, 20L, "content");
        BasicReport basic = basic(FiveElement.FIRE);
        Onboarding onboarding = onboarding(List.of(ConcernType.CAREER));

        when(resultParser.parseBasic("content")).thenReturn(basic);
        when(onboardingRepository.findById(20L)).thenReturn(Optional.of(onboarding));
        when(dailyElementProvider.todayElement()).thenReturn(ElementType.WOOD);

        RecommendationScoringContext context = service().prepare(report).orElseThrow();

        assertThat(context.reportId()).isEqualTo(10L);
        assertThat(context.basicReport()).isEqualTo(basic);
        assertThat(context.neededElement()).isEqualTo(ElementType.FIRE);
        assertThat(context.dailyElement()).isEqualTo(ElementType.WOOD);
        assertThat(context.concerns()).containsExactly(ConcernType.CAREER);
        verify(resultParser).parseBasic("content");
        verify(onboardingRepository).findById(20L);
        verify(dailyElementProvider).todayElement();
    }

    @Test
    void scoreUsesPreparedContextWithoutRepositoryOrParserLookups() {
        RecommendationScoringContext context = new RecommendationScoringContext(
                10L,
                basic(FiveElement.FIRE),
                ElementType.FIRE,
                ElementType.FIRE,
                List.of(ConcernType.CAREER)
        );
        Place place = place(ElementType.FIRE, 30);

        RecommendationMatchContext match = service().score(context, place);

        assertThat(match.reportId()).isEqualTo(10L);
        assertThat(match.elementScore()).isEqualTo(45);
        assertThat(match.concernScore()).isEqualTo(30);
        assertThat(match.dailyScore()).isEqualTo(25);
        assertThat(match.totalScore()).isEqualTo(100);
        verify(fortuneReportRepository, never()).findById(10L);
        verifyNoLookupCollaborators();
    }

    private void verifyNoLookupCollaborators() {
        verify(resultParser, never()).parseBasic(org.mockito.ArgumentMatchers.any());
        verify(onboardingRepository, never()).findById(org.mockito.ArgumentMatchers.any());
        verify(dailyElementProvider, never()).todayElement();
    }

    private RecommendationMatchingService service() {
        return new RecommendationMatchingService(
                fortuneReportRepository,
                onboardingRepository,
                resultParser,
                dailyElementProvider
        );
    }

    private FortuneReport report(Long id, Long onboardingId, String reportContent) {
        try {
            Constructor<FortuneReport> constructor = FortuneReport.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FortuneReport report = constructor.newInstance();
            ReflectionTestUtils.setField(report, "id", id);
            ReflectionTestUtils.setField(report, "onboardingId", onboardingId);
            ReflectionTestUtils.setField(report, "reportContent", reportContent);
            return report;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Onboarding onboarding(List<ConcernType> concerns) {
        try {
            Constructor<Onboarding> constructor = Onboarding.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Onboarding onboarding = constructor.newInstance();
            ReflectionTestUtils.setField(onboarding, "concernTypes", concerns);
            return onboarding;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private BasicReport basic(FiveElement complementElement) {
        return new BasicReport(
                "type title",
                "type name",
                "summary",
                List.of(FiveElement.WATER),
                complementElement,
                List.of(),
                List.of()
        );
    }

    private Place place(ElementType elementType, int careerScore) {
        return Place.builder()
                .name("place")
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.0)
                .longitude(127.0)
                .elementType(elementType)
                .themeType(ThemeType.CAREER)
                .averageRating(4.0)
                .reviewCount(0)
                .editorPick(false)
                .active(true)
                .terrainType("terrain")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(careerScore)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
    }
}
