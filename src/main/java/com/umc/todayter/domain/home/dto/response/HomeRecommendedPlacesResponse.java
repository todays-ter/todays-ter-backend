package com.umc.todayter.domain.home.dto.response;

import com.umc.todayter.global.security.context.CurrentUserType;

import java.util.List;

public record HomeRecommendedPlacesResponse(
        CurrentUserType userType,
        boolean isLimited,
        int visibleCount,
        int totalCount,
        List<HomeRecommendedPlaceItemResponse> recommendations,
        HomeLoginPromptResponse loginPrompt
) {

    public HomeRecommendedPlacesResponse {
        recommendations = List.copyOf(recommendations);
    }
}
