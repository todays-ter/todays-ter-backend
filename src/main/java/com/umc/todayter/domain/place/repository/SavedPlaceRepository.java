package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {

    List<SavedPlace> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
