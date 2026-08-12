package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.entity.PlaceRecommendationSnapshot;
import com.umc.todayter.domain.place.repository.PlaceRecommendationSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceRecommendationSnapshotTransactionService {

    private final PlaceRecommendationSnapshotRepository snapshotRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<PlaceRecommendationSnapshot> findCached(Long fortuneReportId, Long placeId, String concernKey) {
        return snapshotRepository.findFirstByFortuneReportIdAndPlaceIdAndConcernKeyOrderByIdDesc(
                fortuneReportId, placeId, concernKey
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlaceRecommendationSnapshot saveSnapshot(PlaceRecommendationSnapshot snapshot) {
        return snapshotRepository.saveAndFlush(snapshot);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<PlaceRecommendationSnapshot> findCachedAfterSaveConflict(
            Long fortuneReportId,
            Long placeId,
            String concernKey
    ) {
        return findCached(fortuneReportId, placeId, concernKey);
    }

    @Transactional(readOnly = true)
    public Optional<PlaceRecommendationSnapshot> findByShareToken(String shareToken) {
        return snapshotRepository.findByShareToken(shareToken);
    }

    @Transactional(readOnly = true)
    public boolean existsByShareToken(String shareToken) {
        return snapshotRepository.existsByShareToken(shareToken);
    }
}
