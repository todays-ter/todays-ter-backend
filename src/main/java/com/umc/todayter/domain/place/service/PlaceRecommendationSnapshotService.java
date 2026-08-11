package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.onboarding.enums.ConcernType;
import com.umc.todayter.domain.place.dto.internal.PlaceRecommendationAiContent;
import com.umc.todayter.domain.place.dto.internal.RecommendationMatchContext;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import com.umc.todayter.domain.place.exception.PlaceErrorCode;
import com.umc.todayter.domain.place.service.provider.PlaceRecommendationAiContentParser;
import com.umc.todayter.domain.place.service.provider.PlaceRecommendationPromptProvider;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.config.client.OpenAiFortuneReportClient;
import com.umc.todayter.global.security.context.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceRecommendationSnapshotService {

    private final PlaceRecommendationSnapshotTransactionService transactionService;
    private final RecommendationMatchingService matchingService;
    private final PlaceRecommendationPromptProvider promptProvider;
    private final PlaceRecommendationAiContentParser contentParser;
    private final OpenAiFortuneReportClient openAiClient;
    private final Clock clock;

    public Optional<PlaceRecommendationSnapshot> getOrCreate(CurrentUserContext userContext, Place place) {
        return matchingService.resolve(userContext, place)
                .map(match -> getOrCreate(match, place));
    }

    public PlaceRecommendationSnapshot getOrCreateRequired(CurrentUserContext userContext, Place place) {
        return getOrCreate(userContext, place)
                .orElseThrow(() -> new CustomException(PlaceErrorCode.PERSONALIZED_RECOMMENDATION_UNAVAILABLE));
    }

    public PlaceRecommendationSnapshot enableSharing(PlaceRecommendationSnapshot snapshot) {
        if (snapshot.getShareToken() == null) {
            snapshot.enableSharing(createUniqueShareToken());
            return transactionService.saveSnapshot(snapshot);
        }
        return snapshot;
    }

    public PlaceRecommendationSnapshot getShared(String shareToken) {
        if (shareToken == null || shareToken.length() != 32) {
            throw new CustomException(PlaceErrorCode.SHARED_RECOMMENDATION_NOT_FOUND);
        }
        return transactionService.findByShareToken(shareToken)
                .orElseThrow(() -> new CustomException(PlaceErrorCode.SHARED_RECOMMENDATION_NOT_FOUND));
    }

    public PlaceRecommendationSnapshot getOrCreate(RecommendationMatchContext match, Place place) {
        LocalDate today = LocalDate.now(clock);
        String concernKey = concernKey(match.concerns());
        Optional<PlaceRecommendationSnapshot> cached = transactionService.findCached(
                match.reportId(), place.getId(), concernKey
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        PlaceRecommendationAiContent content = contentParser.parse(
                openAiClient.generate(promptProvider.create(match, place))
        );
        PlaceRecommendationSnapshot snapshot = PlaceRecommendationSnapshot.create(
                match.reportId(),
                place.getId(),
                today,
                concernKey,
                match.totalScore(),
                matchingPoints(match),
                content.whyItMatches(),
                content.actionSuggestion()
        );

        try {
            return transactionService.saveSnapshot(snapshot);
        } catch (DataIntegrityViolationException e) {
            return transactionService.findCachedAfterSaveConflict(match.reportId(), place.getId(), concernKey)
                    .orElseThrow(() -> e);
        }
    }

    private List<String> matchingPoints(RecommendationMatchContext match) {
        String primaryElement = match.basicReport().primaryElements().isEmpty()
                ? match.neededElement().getDisplayName()
                : match.basicReport().primaryElements().get(0).getLabel();
        String concernPoint = match.concerns().stream()
                .findFirst()
                .map(this::concernPoint)
                .orElse("오늘의 균형");
        return List.of(
                "주 오행 " + primaryElement,
                dailyPoint(match),
                concernPoint
        );
    }

    private String dailyPoint(RecommendationMatchContext match) {
        if (match.dailyScore() == 25) {
            return "오늘 흐름 일치";
        }
        if (match.dailyScore() >= 16) {
            return "오늘 흐름 상생";
        }
        return "오늘 흐름 조율";
    }

    private String concernPoint(ConcernType concern) {
        return switch (concern) {
            case LOVE -> "연애운 회복";
            case RELATIONSHIP -> "관계 흐름 조율";
            case CAREER -> "일·커리어 집중";
            case WEALTH -> "재물 흐름 정리";
            case HEALTH -> "휴식·회복";
            case OTHER -> "전환·자기정리";
        };
    }

    private String concernKey(List<ConcernType> concerns) {
        if (concerns == null || concerns.isEmpty()) {
            return "NONE";
        }
        return concerns.stream()
                .sorted(Comparator.comparing(Enum::name))
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    private String createUniqueShareToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (transactionService.existsByShareToken(token));
        return token;
    }
}
