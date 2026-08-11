package com.umc.todayter.domain.mypage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MyPageNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private boolean isPushEnabled;

    private boolean isMarketingEnabled;

    private boolean isNightMarketingEnabled;

    private LocalDateTime updatedAt;

    public void updateSettings(boolean isPushEnabled, boolean isMarketingEnabled, boolean isNightMarketingEnabled) {
        this.isPushEnabled = isPushEnabled;
        this.isMarketingEnabled = isMarketingEnabled;
        this.isNightMarketingEnabled = isNightMarketingEnabled;
        this.updatedAt = LocalDateTime.now();
    }
}