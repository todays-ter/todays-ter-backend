package com.umc.todayter.domain.place.entity;

import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "place_recommendation_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_place_recommendation_cache",
                columnNames = {"fortune_report_id", "place_id", "recommendation_date", "concern_key"}
        ),
        indexes = @Index(
                name = "idx_place_recommendation_share_token",
                columnList = "share_token",
                unique = true
        )
)
public class PlaceRecommendationSnapshot extends BaseEntity {

    @Column(name = "fortune_report_id", nullable = false)
    private Long fortuneReportId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Column(name = "concern_key", nullable = false, length = 255)
    private String concernKey;

    @Column(name = "matching_score", nullable = false)
    private Integer matchingScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matching_points", nullable = false, columnDefinition = "json")
    private List<String> matchingPoints;

    @Column(name = "why_it_matches", nullable = false, columnDefinition = "text")
    private String whyItMatches;

    @Column(name = "action_suggestion", nullable = false, columnDefinition = "text")
    private String actionSuggestion;

    @Column(name = "share_token", length = 32, unique = true)
    private String shareToken;

    public static PlaceRecommendationSnapshot create(
            Long fortuneReportId,
            Long placeId,
            LocalDate recommendationDate,
            String concernKey,
            int matchingScore,
            List<String> matchingPoints,
            String whyItMatches,
            String actionSuggestion
    ) {
        PlaceRecommendationSnapshot snapshot = new PlaceRecommendationSnapshot();
        snapshot.fortuneReportId = fortuneReportId;
        snapshot.placeId = placeId;
        snapshot.recommendationDate = recommendationDate;
        snapshot.concernKey = concernKey;
        snapshot.matchingScore = matchingScore;
        snapshot.matchingPoints = List.copyOf(matchingPoints);
        snapshot.whyItMatches = whyItMatches;
        snapshot.actionSuggestion = actionSuggestion;
        return snapshot;
    }

    public void enableSharing(String shareToken) {
        if (shareToken == null || shareToken.length() != 32) {
            throw new IllegalArgumentException("공유 토큰 형식이 올바르지 않습니다.");
        }
        if (this.shareToken == null) {
            this.shareToken = shareToken;
        }
    }
}
