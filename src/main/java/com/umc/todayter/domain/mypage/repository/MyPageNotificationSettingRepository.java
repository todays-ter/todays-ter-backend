package com.umc.todayter.domain.mypage.repository;

import com.umc.todayter.domain.mypage.entity.MyPageNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyPageNotificationSettingRepository extends JpaRepository<MyPageNotificationSetting, Long> {
    Optional<MyPageNotificationSetting> findByUserId(Long userId);
}