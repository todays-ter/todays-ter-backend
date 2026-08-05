package com.umc.todayter.domain.place.dto.internal;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.BasicReport;
import com.umc.todayter.domain.onboarding.enums.ConcernType;
import com.umc.todayter.domain.place.enums.ElementType;

import java.util.List;

public record RecommendationMatchContext(
        Long reportId,
        BasicReport basicReport,
        ElementType neededElement,
        ElementType dailyElement,
        List<ConcernType> concerns,
        int elementScore,
        int concernScore,
        int dailyScore
) {
    public int totalScore() {
        return Math.max(0, Math.min(elementScore + concernScore + dailyScore, 100));
    }
}
