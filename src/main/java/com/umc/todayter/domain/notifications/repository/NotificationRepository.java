package com.umc.todayter.domain.notifications.repository;

import com.umc.todayter.domain.notifications.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 첫 페이지 조회 (cursor가 null일 때)
    List<Notification> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    // 커서 기반 페이징 (cursor보다 작은 ID만 조회)
    List<Notification> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    int countByUserIdAndIsReadFalse(Long userId);

    boolean existsByDeduplicationKey(String deduplicationKey);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
