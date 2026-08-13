package com.umc.todayter.domain.notifications.config;

import com.umc.todayter.domain.notifications.entity.NotificationActivityType;
import com.umc.todayter.domain.notifications.service.NotificationActivityService;
import com.umc.todayter.domain.place.dto.response.PlaceBookmarkResponse;
import com.umc.todayter.domain.place.dto.response.RecommendationPlaceDetailResponse;
import com.umc.todayter.global.security.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificationActivityAspectTest {

    private final NotificationActivityService activityService = mock(NotificationActivityService.class);
    private final NotificationActivityAspect aspect = new NotificationActivityAspect(activityService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedRecommendationViewIsRecorded() {
        authenticate(1L);
        RecommendationPlaceDetailResponse response = new RecommendationPlaceDetailResponse(
                10L, "테스트 터", null, null, List.of(), 90,
                List.of(), null, null, null, false
        );

        aspect.recordRecommendationViewed(response);

        verify(activityService).record(1L, 10L, NotificationActivityType.RECOMMENDATION_VIEWED);
    }

    @Test
    void bookmarkRemovalIsNotRecorded() {
        authenticate(1L);

        aspect.recordPlaceSaved(new PlaceBookmarkResponse(10L, false));

        verifyNoInteractions(activityService);
    }

    private void authenticate(Long memberId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthPrincipal(memberId), null, List.of())
        );
    }
}
