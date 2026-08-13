package com.umc.todayter.domain.notifications.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.todayter.domain.notifications.entity.NotificationType;
import com.umc.todayter.domain.notifications.entity.RemindCycle;
import com.umc.todayter.domain.notifications.entity.RemindTime;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationResponseDTOTest {

    @Test
    void serializesFrontendFieldNamesAndTimezoneOffset() {
        NotificationResponseDTO.NotificationDTO response =
                NotificationResponseDTO.NotificationDTO.builder()
                        .notificationId(102L)
                        .type(NotificationType.REMIND)
                        .title("오늘의 터 리마인드")
                        .content("아직 오늘의 추천을 확인하지 않았어요.")
                        .isRead(false)
                        .createdAt(OffsetDateTime.parse("2026-08-10T09:00:00+09:00"))
                        .build();

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.has("isRead")).isTrue();
        assertThat(json.has("read")).isFalse();
        assertThat(json.get("createdAt").asText()).endsWith("+09:00");
    }

    @Test
    void serializesNotificationSettingWithSpecifiedIsPrefix() {
        NotificationResponseDTO.NotificationSettingDTO response =
                NotificationResponseDTO.NotificationSettingDTO.builder()
                        .isTodayRemind(true)
                        .remindCycle(RemindCycle.EVERY_2_DAYS)
                        .remindTime(RemindTime.SIX_PM)
                        .isSavedPlace(true)
                        .isServiceNotice(true)
                        .isMarketing(false)
                        .build();

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.get("isTodayRemind").asBoolean()).isTrue();
        assertThat(json.get("remindCycle").asText()).isEqualTo("EVERY_2_DAYS");
        assertThat(json.get("remindTime").asText()).isEqualTo("18:00");
        assertThat(json.get("isSavedPlace").asBoolean()).isTrue();
        assertThat(json.get("isServiceNotice").asBoolean()).isTrue();
        assertThat(json.get("isMarketing").asBoolean()).isFalse();
        assertThat(json.has("todayRemind")).isFalse();
    }
}
