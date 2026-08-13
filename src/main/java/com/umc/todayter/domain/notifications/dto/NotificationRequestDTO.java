package com.umc.todayter.domain.notifications.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.umc.todayter.domain.notifications.entity.RemindCycle;
import com.umc.todayter.domain.notifications.entity.RemindTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

public final class NotificationRequestDTO {

    private NotificationRequestDTO() {
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "알림 설정 부분 변경 요청. 변경할 필드만 전송할 수 있습니다.")
    public static class UpdateNotificationSettingDTO {

        @JsonProperty("isTodayRemind")
        @JsonAlias("todayRemind")
        @Schema(description = "오늘의 터 리마인드 수신 여부", example = "true")
        private Boolean todayRemind;

        @Schema(description = "리마인드 주기", example = "EVERY_2_DAYS")
        private RemindCycle remindCycle;

        @Schema(description = "리마인드 발송 시각", example = "18:00")
        private RemindTime remindTime;

        @JsonProperty("isSavedPlace")
        @JsonAlias("savedPlace")
        @Schema(description = "저장한 터 알림 수신 여부", example = "true")
        private Boolean savedPlace;

        @JsonProperty("isServiceNotice")
        @JsonAlias("serviceNotice")
        @Schema(description = "서비스 중요 알림 수신 여부", example = "true")
        private Boolean serviceNotice;

        @JsonProperty("isMarketing")
        @JsonAlias("marketing")
        @Schema(description = "마케팅 정보 수신 여부", example = "false")
        private Boolean marketing;
    }
}
