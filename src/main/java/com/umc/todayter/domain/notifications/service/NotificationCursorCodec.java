package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class NotificationCursorCodec {

    public String encode(Long notificationId) {
        if (notificationId == null || notificationId <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(Long.toString(notificationId).getBytes(StandardCharsets.UTF_8));
    }

    public Long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );
            long notificationId = Long.parseLong(decoded);
            if (notificationId <= 0) {
                throw new IllegalArgumentException("cursor must be positive");
            }
            return notificationId;
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }
}
