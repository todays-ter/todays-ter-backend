package com.umc.todayter.domain.record.repository;

import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.enums.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    boolean existsByMemberIdAndPlaceIdAndType(Long memberId, Long placeId, RecordType type);
    boolean existsByMemberIdAndPlaceId(Long memberId, Long placeId);
    long countByPlaceIdAndType(Long placeId, RecordType type);

    List<VisitRecord> findByPlaceIdAndTypeOrderByCreatedAtDesc(Long placeId, RecordType type);

    @Query("select avg(vr.rating) from VisitRecord vr where vr.place.id = :placeId and vr.type = com.umc.todayter.domain.record.enums.RecordType.REVIEW")
    Double findAverageRatingByPlaceId(@Param("placeId") Long placeId);

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
