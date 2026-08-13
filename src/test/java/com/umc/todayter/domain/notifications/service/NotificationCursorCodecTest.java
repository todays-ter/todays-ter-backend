package com.umc.todayter.domain.notifications.service;

import com.umc.todayter.global.apiPayload.exception.CustomException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationCursorCodecTest {

    private final NotificationCursorCodec codec = new NotificationCursorCodec();

    @Test
    void cursorRoundTripHidesNumericNotificationId() {
        String cursor = codec.encode(102L);

        assertThat(cursor).isEqualTo("MTAy");
        assertThat(codec.decode(cursor)).isEqualTo(102L);
    }

    @Test
    void invalidCursorReturnsCommonBadRequestException() {
        assertThatThrownBy(() -> codec.decode("not-a-valid-cursor"))
                .isInstanceOf(CustomException.class);
    }
}
