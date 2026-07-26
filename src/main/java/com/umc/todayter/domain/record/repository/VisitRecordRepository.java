package com.umc.todayter.domain.record.repository;

import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.enums.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    boolean existsByMemberIdAndPlaceIdAndType(Long memberId, Long placeId, RecordType type);
}
