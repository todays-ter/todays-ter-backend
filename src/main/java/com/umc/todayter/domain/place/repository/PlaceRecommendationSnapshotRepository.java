package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface PlaceRecommendationSnapshotRepository
        extends JpaRepository<PlaceRecommendationSnapshot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PlaceRecommendationSnapshot> findFirstByFortuneReportIdAndPlaceIdAndConcernKeyOrderByIdDesc(
            Long fortuneReportId,
            Long placeId,
            String concernKey
    );

    Optional<PlaceRecommendationSnapshot> findByShareToken(String shareToken);

    boolean existsByShareToken(String shareToken);
}
