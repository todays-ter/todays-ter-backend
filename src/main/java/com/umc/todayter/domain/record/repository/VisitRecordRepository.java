package com.umc.todayter.domain.record.repository;

import com.umc.todayter.domain.record.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {
}
