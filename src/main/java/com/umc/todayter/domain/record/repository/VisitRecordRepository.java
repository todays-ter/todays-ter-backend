package com.umc.todayter.domain.record.repository;

import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.enums.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    boolean existsByMemberIdAndPlaceIdAndType(Long memberId, Long placeId, RecordType type);

    @Query("""
            select vr from VisitRecord vr
            where vr.member.id = :memberId
            and vr.createdAt = (
                select max(vr2.createdAt) from VisitRecord vr2
                where vr2.member.id = :memberId and vr2.place = vr.place
            )
            """)
    List<VisitRecord> findLatestPerPlaceByMemberId(@Param("memberId") Long memberId);
}
