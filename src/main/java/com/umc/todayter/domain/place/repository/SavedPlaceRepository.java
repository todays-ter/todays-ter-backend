package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {

    List<SavedPlace> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByMemberIdAndPlaceId(Long memberId, Long placeId);

    Optional<SavedPlace> findByMemberIdAndPlaceId(Long memberId, Long placeId);

    long deleteByMemberIdAndPlaceId(Long memberId, Long placeId);
}
