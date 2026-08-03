package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.enums.FortuneReportStatus;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.fortune.service.parser.FortuneReportResultParser;
import com.umc.todayter.domain.onboarding.entity.Onboarding;
import com.umc.todayter.domain.onboarding.enums.ConcernType;
import com.umc.todayter.domain.onboarding.repository.OnboardingRepository;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.dto.internal.RecommendationScoringContext;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.service.provider.DailyFortuneElementProvider;
import com.umc.todayter.global.security.context.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationMatchingService {

    private static final int ELEMENT_EXACT = 45;
    private static final int PLACE_GENERATES_NEEDED = 38;
    private static final int NEEDED_GENERATES_PLACE = 30;
    private static final int NEEDED_CONTROLS_PLACE = 18;
    private static final int PLACE_CONTROLS_NEEDED = 6;

    private static final int DAILY_EXACT = 25;
    private static final int PLACE_GENERATES_DAILY = 21;
    private static final int DAILY_GENERATES_PLACE = 16;
    private static final int DAILY_CONTROLS_PLACE = 9;
    private static final int PLACE_CONTROLS_DAILY = 3;

    private static final Map<ElementType, ElementType> GENERATING_CYCLE = generatingCycle();
    private static final Map<ElementType, ElementType> CONTROLLING_CYCLE = controllingCycle();

    private final FortuneReportRepository fortuneReportRepository;
    private final OnboardingRepository onboardingRepository;
    private final FortuneReportResultParser resultParser;
    private final DailyFortuneElementProvider dailyElementProvider;

    public Integer calculate(CurrentUserContext userContext, Place place) {
        return resolve(userContext, place)
                .map(RecommendationMatchContext::totalScore)
                .orElse(null);
    }

    public Optional<RecommendationMatchContext> resolve(CurrentUserContext userContext, Place place) {
        if (userContext == null) {
            return Optional.empty();
        }

        return findLatestCompletedReport(userContext)
                .flatMap(report -> prepare(report).map(context -> score(context, place)));
    }

    public Optional<RecommendationScoringContext> prepare(FortuneReport report) {
        BasicReport basicReport = resultParser.parseBasic(report.getReportContent());
        ElementType neededElement = toElementType(basicReport.complementElement());
        if (neededElement == null) {
            return Optional.empty();
        }

        List<ConcernType> concerns = onboardingRepository.findById(report.getOnboardingId())
                .map(Onboarding::getConcernTypes)
                .orElse(List.of());

        ElementType dailyElement = dailyElementProvider.todayElement();
        return Optional.of(new RecommendationScoringContext(
                report.getId(),
                basicReport,
                neededElement,
                dailyElement,
                concerns == null ? List.of() : List.copyOf(concerns)
        ));
    }

    public RecommendationMatchContext score(RecommendationScoringContext context, Place place) {
        return new RecommendationMatchContext(
                context.reportId(),
                context.basicReport(),
                context.neededElement(),
                context.dailyElement(),
                context.concerns(),
                elementCompatibilityScore(context.neededElement(), place.getElementType()),
                concernCompatibilityScore(context.concerns(), place),
                dailyCompatibilityScore(context.dailyElement(), place.getElementType())
        );
    }

    private Optional<FortuneReport> findLatestCompletedReport(CurrentUserContext userContext) {
        if (userContext.isMember()) {
            return fortuneReportRepository.findFirstByMemberIdAndStatusOrderByIdDesc(
                    userContext.memberId(), FortuneReportStatus.COMPLETED
            );
        }
        return fortuneReportRepository.findFirstByGuestSessionIdAndStatusOrderByIdDesc(
                userContext.guestSessionId(), FortuneReportStatus.COMPLETED
        );
    }

    int elementCompatibilityScore(ElementType needed, ElementType place) {
        if (needed == place) {
            return ELEMENT_EXACT;
        }
        if (GENERATING_CYCLE.get(place) == needed) {
            return PLACE_GENERATES_NEEDED;
        }
        if (GENERATING_CYCLE.get(needed) == place) {
            return NEEDED_GENERATES_PLACE;
        }
        if (CONTROLLING_CYCLE.get(needed) == place) {
            return NEEDED_CONTROLS_PLACE;
        }
        return PLACE_CONTROLS_NEEDED;
    }

    int dailyCompatibilityScore(ElementType daily, ElementType place) {
        if (daily == place) {
            return DAILY_EXACT;
        }
        if (GENERATING_CYCLE.get(place) == daily) {
            return PLACE_GENERATES_DAILY;
        }
        if (GENERATING_CYCLE.get(daily) == place) {
            return DAILY_GENERATES_PLACE;
        }
        if (CONTROLLING_CYCLE.get(daily) == place) {
            return DAILY_CONTROLS_PLACE;
        }
        return PLACE_CONTROLS_DAILY;
    }

    int concernCompatibilityScore(List<ConcernType> concerns, Place place) {
        if (concerns == null || concerns.isEmpty()) {
            return 0;
        }
        return concerns.stream()
                .mapToInt(concern -> scoreForConcern(concern, place))
                .max()
                .orElse(0);
    }

    private int scoreForConcern(ConcernType concern, Place place) {
        int score = switch (concern) {
            case LOVE -> place.getLoveScore();
            case RELATIONSHIP -> place.getRelationshipScore();
            case CAREER -> place.getCareerScore();
            case WEALTH -> place.getCareerScore();
            case HEALTH -> place.getRestScore();
            case OTHER -> Math.max(place.getStudyScore(), place.getTransitionScore());
        };
        return Math.max(0, Math.min(score, 30));
    }

    private ElementType toElementType(FiveElement element) {
        if (element == null) {
            return null;
        }
        return switch (element) {
            case WOOD -> ElementType.WOOD;
            case FIRE -> ElementType.FIRE;
            case EARTH -> ElementType.EARTH;
            case METAL -> ElementType.METAL;
            case WATER -> ElementType.WATER;
        };
    }

    private static Map<ElementType, ElementType> generatingCycle() {
        Map<ElementType, ElementType> cycle = new EnumMap<>(ElementType.class);
        cycle.put(ElementType.WOOD, ElementType.FIRE);
        cycle.put(ElementType.FIRE, ElementType.EARTH);
        cycle.put(ElementType.EARTH, ElementType.METAL);
        cycle.put(ElementType.METAL, ElementType.WATER);
        cycle.put(ElementType.WATER, ElementType.WOOD);
        return Map.copyOf(cycle);
    }

    private static Map<ElementType, ElementType> controllingCycle() {
        Map<ElementType, ElementType> cycle = new EnumMap<>(ElementType.class);
        cycle.put(ElementType.WOOD, ElementType.EARTH);
        cycle.put(ElementType.EARTH, ElementType.WATER);
        cycle.put(ElementType.WATER, ElementType.FIRE);
        cycle.put(ElementType.FIRE, ElementType.METAL);
        cycle.put(ElementType.METAL, ElementType.WOOD);
        return Map.copyOf(cycle);
    }
}
