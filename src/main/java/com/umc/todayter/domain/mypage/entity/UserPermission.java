package com.umc.todayter.domain.mypage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private boolean isCameraAllowed;

    private boolean isPhotoLibraryAllowed;

    private boolean isLocationAllowed;

    private LocalDateTime updatedAt;

    public void updatePermissions(boolean isCameraAllowed, boolean isPhotoLibraryAllowed, boolean isLocationAllowed) {
        this.isCameraAllowed = isCameraAllowed;
        this.isPhotoLibraryAllowed = isPhotoLibraryAllowed;
        this.isLocationAllowed = isLocationAllowed;
        this.updatedAt = LocalDateTime.now();
    }
}