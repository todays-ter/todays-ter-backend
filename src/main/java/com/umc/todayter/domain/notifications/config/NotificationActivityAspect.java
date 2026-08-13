package com.umc.todayter.domain.notifications.config;

import com.umc.todayter.domain.notifications.entity.NotificationActivityType;
import com.umc.todayter.domain.notifications.service.NotificationActivityService;
import com.umc.todayter.domain.place.dto.response.PlaceBookmarkResponse;
import com.umc.todayter.domain.place.dto.response.RecommendationPlaceDetailResponse;
import com.umc.todayter.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class NotificationActivityAspect {

    private final NotificationActivityService notificationActivityService;

    @AfterReturning(
            pointcut = "execution(* com.umc.todayter.domain.place.service.PlaceService.getRecommendedPlaceDetail(..))",
            returning = "response"
    )
    public void recordRecommendationViewed(RecommendationPlaceDetailResponse response) {
        Long memberId = SecurityUtil.getCurrentMemberIdOrNull();
        if (memberId != null && response != null) {
            notificationActivityService.record(
                    memberId,
                    response.placeId(),
                    NotificationActivityType.RECOMMENDATION_VIEWED
            );
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.umc.todayter.domain.place.service.PlaceService.updateBookmark(..))",
            returning = "response"
    )
    public void recordPlaceSaved(PlaceBookmarkResponse response) {
        Long memberId = SecurityUtil.getCurrentMemberIdOrNull();
        if (memberId != null && response != null && response.isSaved()) {
            notificationActivityService.record(
                    memberId,
                    response.placeId(),
                    NotificationActivityType.PLACE_SAVED
            );
        }
    }
}
