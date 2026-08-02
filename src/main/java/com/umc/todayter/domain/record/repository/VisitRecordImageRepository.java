package com.umc.todayter.domain.record.repository;

import com.umc.todayter.domain.record.entity.VisitRecordImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitRecordImageRepository extends JpaRepository<VisitRecordImage, Long> {

    List<VisitRecordImage> findByVisitRecordIdOrderBySortOrderAsc(Long visitRecordId);

    List<VisitRecordImage> findByVisitRecordIdInOrderBySortOrderAsc(List<Long> visitRecordIds);
}
