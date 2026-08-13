package com.umc.todayter.domain.notifications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.service.MemberService;
import com.umc.todayter.domain.notifications.dto.NotificationRequestDTO;
import com.umc.todayter.domain.notifications.dto.NotificationResponseDTO;
import com.umc.todayter.domain.notifications.entity.Notification;
import com.umc.todayter.domain.notifications.entity.NotificationSetting;
import com.umc.todayter.domain.notifications.entity.NotificationType;
import com.umc.todayter.domain.notifications.repository.NotificationRepository;
import com.umc.todayter.domain.notifications.repository.NotificationSettingRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    private MemberService memberService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private NotificationCursorCodec notificationCursorCodec;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getSettingsCreatesDesignDefaultsWhenSettingDoesNotExist() {
        when(memberService.getActiveMember(MEMBER_ID)).thenReturn(Member.create("오늘이"));
        when(notificationSettingRepository.findByUserId(MEMBER_ID)).thenReturn(Optional.empty());
        when(notificationSettingRepository.save(any(NotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponseDTO.NotificationSettingDTO result =
                notificationService.getNotificationSettings(MEMBER_ID);

        assertThat(result.isTodayRemind()).isTrue();
        assertThat(result.getRemindCycle().getValue()).isEqualTo("EVERY_2_DAYS");
        assertThat(result.getRemindTime().getValue()).isEqualTo("18:00");
        assertThat(result.isSavedPlace()).isTrue();
        assertThat(result.isServiceNotice()).isTrue();
        assertThat(result.isMarketing()).isTrue();
    }

    @Test
    void patchChangesOnlyFieldsIncludedInRequest() throws Exception {
        NotificationSetting setting = NotificationSetting.createDefault(MEMBER_ID);
        NotificationRequestDTO.UpdateNotificationSettingDTO request =
                new ObjectMapper().readValue(
                        "{\"marketing\":false}",
                        NotificationRequestDTO.UpdateNotificationSettingDTO.class
                );
        when(memberService.getActiveMember(MEMBER_ID)).thenReturn(Member.create("오늘이"));
        when(notificationSettingRepository.findByUserId(MEMBER_ID)).thenReturn(Optional.of(setting));

        NotificationResponseDTO.NotificationSettingDTO result =
                notificationService.updateNotificationSettings(MEMBER_ID, request);

        assertThat(result.isTodayRemind()).isTrue();
        assertThat(result.isSavedPlace()).isTrue();
        assertThat(result.isServiceNotice()).isTrue();
        assertThat(result.isMarketing()).isFalse();
    }

    @Test
    void listUsesOpaqueCursorAndReturnsKoreaOffset() {
        Notification first = notification(11L, LocalDateTime.of(2026, 8, 13, 18, 0));
        Notification second = notification(10L, LocalDateTime.of(2026, 8, 13, 17, 0));
        when(memberService.getActiveMember(MEMBER_ID)).thenReturn(Member.create("오늘이"));
        when(notificationCursorCodec.decode("cursor")).thenReturn(20L);
        when(notificationRepository.findByUserIdAndIdLessThanOrderByIdDesc(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                org.mockito.ArgumentMatchers.eq(20L),
                any(Pageable.class)
        )).thenReturn(List.of(first, second));
        when(notificationCursorCodec.encode(11L)).thenReturn("next-cursor");

        NotificationResponseDTO.NotificationListResultDTO result =
                notificationService.getNotifications(MEMBER_ID, "cursor", 1);

        assertThat(result.getNotifications()).singleElement().satisfies(item -> {
            assertThat(item.getNotificationId()).isEqualTo(11L);
            assertThat(item.getType()).isEqualTo(NotificationType.REMIND);
            assertThat(item.getCreatedAt().getOffset().getTotalSeconds()).isEqualTo(9 * 60 * 60);
        });
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("next-cursor");
    }

    private Notification notification(Long id, LocalDateTime createdAt) {
        Notification notification = Notification.create(
                MEMBER_ID,
                "제목",
                "내용",
                NotificationType.REMIND,
                createdAt
        );
        ReflectionTestUtils.setField(notification, "id", id);
        return notification;
    }
}
