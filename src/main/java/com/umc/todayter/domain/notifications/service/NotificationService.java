package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.domain.member.service.MemberService;
import com.umc.todayter.domain.notifications.dto.NotificationRequestDTO;
import com.umc.todayter.domain.notifications.dto.NotificationResponseDTO;
import com.umc.todayter.domain.notifications.entity.Notification;
import com.umc.todayter.domain.notifications.entity.NotificationSetting;
import com.umc.todayter.domain.notifications.exception.NotificationErrorCode;
import com.umc.todayter.domain.notifications.repository.NotificationRepository;
import com.umc.todayter.domain.notifications.repository.NotificationSettingRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final MemberService memberService;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationCursorCodec notificationCursorCodec;

    public NotificationResponseDTO.NotificationListResultDTO getNotifications(
            Long memberId,
            String cursor,
            int size
    ) {
        memberService.getActiveMember(memberId);
        Pageable pageable = PageRequest.of(0, size + 1);
        Long cursorId = notificationCursorCodec.decode(cursor);

        List<Notification> notifications = cursorId == null
                ? notificationRepository.findByUserIdOrderByIdDesc(memberId, pageable)
                : notificationRepository.findByUserIdAndIdLessThanOrderByIdDesc(memberId, cursorId, pageable);

        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size);
        }

        String nextCursor = hasNext && !notifications.isEmpty()
                ? notificationCursorCodec.encode(notifications.get(notifications.size() - 1).getId())
                : null;
        List<NotificationResponseDTO.NotificationDTO> notificationList = notifications.stream()
                .map(this::toNotificationDTO)
                .toList();

        return NotificationResponseDTO.NotificationListResultDTO.builder()
                .notifications(notificationList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    public NotificationResponseDTO.UnreadCountResultDTO getUnreadCount(Long memberId) {
        memberService.getActiveMember(memberId);
        return NotificationResponseDTO.UnreadCountResultDTO.builder()
                .unreadCount(notificationRepository.countByUserIdAndIsReadFalse(memberId))
                .build();
    }

    @Transactional
    public NotificationResponseDTO.ReadResultDTO readNotification(Long memberId, Long notificationId) {
        memberService.getActiveMember(memberId);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, memberId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead();

        return NotificationResponseDTO.ReadResultDTO.builder()
                .notificationId(notification.getId())
                .build();
    }

    @Transactional
    public NotificationResponseDTO.ReadAllResultDTO readAllNotifications(Long memberId) {
        memberService.getActiveMember(memberId);
        int updatedCount = notificationRepository.markAllAsReadByUserId(memberId);
        return NotificationResponseDTO.ReadAllResultDTO.builder()
                .updatedCount(updatedCount)
                .build();
    }

    @Transactional
    public NotificationResponseDTO.NotificationSettingDTO getNotificationSettings(Long memberId) {
        memberService.getActiveMember(memberId);
        return toSettingDTO(getOrCreateNotificationSetting(memberId));
    }

    @Transactional
    public NotificationResponseDTO.NotificationSettingDTO updateNotificationSettings(
            Long memberId,
            NotificationRequestDTO.UpdateNotificationSettingDTO request
    ) {
        memberService.getActiveMember(memberId);
        NotificationSetting setting = getOrCreateNotificationSetting(memberId);
        setting.updateSettings(
                request.getTodayRemind(),
                request.getRemindCycle(),
                request.getRemindTime(),
                request.getSavedPlace(),
                request.getServiceNotice(),
                request.getMarketing()
        );
        return toSettingDTO(setting);
    }

    private NotificationSetting getOrCreateNotificationSetting(Long memberId) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(memberId)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.createDefault(memberId)
                ));
        setting.repairLegacyDefaults();
        return setting;
    }

    private NotificationResponseDTO.NotificationDTO toNotificationDTO(Notification notification) {
        return NotificationResponseDTO.NotificationDTO.builder()
                .notificationId(notification.getId())
                .placeId(notification.getPlaceId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt().atZone(KOREA_ZONE).toOffsetDateTime())
                .build();
    }

    private NotificationResponseDTO.NotificationSettingDTO toSettingDTO(NotificationSetting setting) {
        setting.repairLegacyDefaults();
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
