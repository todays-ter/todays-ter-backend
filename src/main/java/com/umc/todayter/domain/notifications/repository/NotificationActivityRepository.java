package com.umc.todayter.domain.notifications.repository;

import com.umc.todayter.domain.notifications.entity.NotificationActivity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationActivityRepository extends JpaRepository<NotificationActivity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<NotificationActivity> findByUserIdAndPlaceId(Long userId, Long placeId);

    boolean existsByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select activity from NotificationActivity activity
            where activity.processedAt is null
              and activity.dueAt <= :now
            order by activity.dueAt asc, activity.id asc
            """)
    List<NotificationActivity> findDueActivities(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
