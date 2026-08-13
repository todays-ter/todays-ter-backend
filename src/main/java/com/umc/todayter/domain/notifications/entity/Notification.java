package com.umc.todayter.domain.notifications.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification",
        indexes = @Index(name = "idx_notification_user_id_id", columnList = "user_id,id"),
        uniqueConstraints = @jakarta.persistence.UniqueConstraint(
                name = "uk_notification_deduplication_key",
                columnNames = "deduplication_key"
        )
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "crew_id")
    private Long crewId;

    @Column(name = "place_id")
    private Long placeId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deduplication_key", length = 160, unique = true)
    private String deduplicationKey;

    private Notification(
            Long userId,
            Long placeId,
            String title,
            String content,
            NotificationType type,
            LocalDateTime createdAt,
            String deduplicationKey
    ) {
        this.userId = userId;
        this.placeId = placeId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isRead = false;
        this.createdAt = createdAt;
        this.deduplicationKey = deduplicationKey;
    }

    public static Notification create(
            Long userId,
            String title,
            String content,
            NotificationType type,
            LocalDateTime createdAt
    ) {
        return create(userId, null, title, content, type, createdAt, null);
    }

    public static Notification create(
            Long userId,
            Long placeId,
            String title,
            String content,
            NotificationType type,
            LocalDateTime createdAt,
            String deduplicationKey
    ) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (title == null || title.isBlank() || content == null || content.isBlank()
                || type == null || createdAt == null) {
            throw new IllegalArgumentException("알림 필수 정보가 누락되었습니다.");
        }
        return new Notification(userId, placeId, title, content, type, createdAt, deduplicationKey);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
