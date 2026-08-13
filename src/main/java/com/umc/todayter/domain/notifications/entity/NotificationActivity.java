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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_activities",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_activity_member_place",
                columnNames = {"user_id", "place_id"}
        ),
        indexes = @Index(
                name = "idx_notification_activity_due",
                columnList = "processed_at,due_at"
        )
)
public class NotificationActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private NotificationActivityType activityType;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public static NotificationActivity create(
            Long userId,
            Long placeId,
            NotificationActivityType activityType,
            LocalDateTime occurredAt,
            LocalDateTime dueAt
    ) {
        NotificationActivity activity = new NotificationActivity();
        activity.userId = userId;
        activity.placeId = placeId;
        activity.activityType = activityType;
        activity.occurredAt = occurredAt;
        activity.dueAt = dueAt;
        return activity;
    }

    public void reschedule(
            NotificationActivityType activityType,
            LocalDateTime occurredAt,
            LocalDateTime dueAt
    ) {
        if (processedAt != null) {
            return;
        }
        this.activityType = activityType;
        this.occurredAt = occurredAt;
        this.dueAt = dueAt;
    }

    public void markProcessed(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
