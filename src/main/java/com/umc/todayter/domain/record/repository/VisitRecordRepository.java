package com.umc.todayter.domain.record.repository;

import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    boolean existsByMemberIdAndPlaceIdAndType(Long memberId, Long placeId, RecordType type);

    Page<VisitRecord> findAllByMemberId(Long memberId, Pageable pageable);

    Page<VisitRecord> findAllByMemberIdAndType(Long memberId, RecordType type, Pageable pageable);
}
