package com.umc.todayter.domain.place.controller;

import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.service.PlaceService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place", description = "장소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "탐색 필터 조회", description = "지역, 테마, 오행 탐색 필터를 조회합니다.")
    @SecurityRequirements
    @GetMapping("/explore-filters")
    public ResponseEntity<ApiResponse<ExploreFiltersResponse>> getExploreFilters() {
        ExploreFiltersResponse result = placeService.getExploreFilters();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }
}
