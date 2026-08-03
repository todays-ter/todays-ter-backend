package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PlaceRecommendationSnapshotRepository
        extends JpaRepository<PlaceRecommendationSnapshot, Long> {

    Optional<PlaceRecommendationSnapshot> findByFortuneReportIdAndPlaceIdAndRecommendationDateAndConcernKey(
            Long fortuneReportId,
            Long placeId,
            LocalDate recommendationDate,
            String concernKey
    );

    Optional<PlaceRecommendationSnapshot> findByShareToken(String shareToken);

    boolean existsByShareToken(String shareToken);
}
