package com.umc.todayter.domain.mypage.repository;

import com.umc.todayter.domain.mypage.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    Optional<UserPermission> findByUserId(Long userId);
}