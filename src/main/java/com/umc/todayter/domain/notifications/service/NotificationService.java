package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.domain.notifications.dto.NotificationRequestDTO;
import com.umc.todayter.domain.notifications.dto.NotificationResponseDTO;
import com.umc.todayter.domain.notifications.entity.Notification;
import com.umc.todayter.domain.notifications.entity.NotificationSetting;
import com.umc.todayter.domain.notifications.entity.RemindCycle;
import com.umc.todayter.domain.notifications.entity.RemindTime;
import com.umc.todayter.domain.notifications.repository.NotificationRepository;
import com.umc.todayter.domain.notifications.repository.NotificationSettingRepository;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    // 1. 알림 목록 조회 (커서 페이징)
    public NotificationResponseDTO.NotificationListResultDTO getNotifications(Long userId, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Notification> notifications;
        if (cursor == null || cursor == 0) {
            notifications = notificationRepository.findByUserIdOrderByIdDesc(userId, pageable);
        } else {
            notifications = notificationRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, pageable);
        }

        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size);
        }

        Long nextCursor = hasNext ? notifications.get(notifications.size() - 1).getId() : null;

        List<NotificationResponseDTO.NotificationDTO> dtoList = notifications.stream()
                .map(n -> NotificationResponseDTO.NotificationDTO.builder()
                        .notificationId(n.getId())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .type(n.getType())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();

        return NotificationResponseDTO.NotificationListResultDTO.builder()
                .notificationList(dtoList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    // 2. 미읽음 알림 개수 조회
    public NotificationResponseDTO.UnreadCountResultDTO getUnreadCount(Long userId) {
        int count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return NotificationResponseDTO.UnreadCountResultDTO.builder()
                .unreadCount(count)
                .build();
    }

   // 3. 알림 단건 읽음 처리
@Transactional
public NotificationResponseDTO.ReadResultDTO readNotification(Long userId, Long notificationId) {
    Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND)); // CustomException으로 변경

    notification.markAsRead();

    return NotificationResponseDTO.ReadResultDTO.builder()
            .notificationId(notification.getId())
            .build();
}

    // 4. 전체 알림 읽음 처리
    @Transactional
    public NotificationResponseDTO.ReadAllResultDTO readAllNotifications(Long userId) {
        int updatedCount = notificationRepository.markAllAsReadByUserId(userId);
        return NotificationResponseDTO.ReadAllResultDTO.builder()
                .updatedCount(updatedCount)
                .build();
    }

    // 5. 알림 설정 정보 조회
    public NotificationResponseDTO.NotificationSettingDTO getNotificationSettings(Long userId) {
        NotificationSetting setting = getOrCreateNotificationSetting(userId);
        return convertToSettingDTO(setting);
    }

    // 6. 알림 설정 수정
    @Transactional
    public NotificationResponseDTO.NotificationSettingDTO updateNotificationSettings(
            Long userId, NotificationRequestDTO.UpdateNotificationSettingDTO request) {

        NotificationSetting setting = getOrCreateNotificationSetting(userId);
        setting.updateSettings(
                request.isTodayRemind(),
                request.getRemindCycle(),
                request.getRemindTime(),
                request.isSavedPlace(),
                request.isServiceNotice(),
                request.isMarketing()
        );

        return convertToSettingDTO(setting);
    }

    private NotificationSetting getOrCreateNotificationSetting(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.builder()
                                .userId(userId)
                                .isTodayRemind(true)
                                .remindCycle(RemindCycle.EVERY_2_DAYS)
                                .remindTime(RemindTime.SIX_PM)
                                .isSavedPlace(true)
                                .isServiceNotice(true)
                                .isMarketing(true)
                                .build()
                ));
    }

    private NotificationResponseDTO.NotificationSettingDTO convertToSettingDTO(NotificationSetting setting) {
        return NotificationResponseDTO.NotificationSettingDTO.builder()
                .isTodayRemind(setting.isTodayRemind())
                .remindCycle(setting.getRemindCycle())
                .remindTime(setting.getRemindTime())
                .isSavedPlace(setting.isSavedPlace())
                .isServiceNotice(setting.isServiceNotice())
                .isMarketing(setting.isMarketing())
                .build();
    }
}