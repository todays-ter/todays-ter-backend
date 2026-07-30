package com.umc.todayter.domain.place.controller;

import com.umc.todayter.domain.place.dto.request.PlaceSearchRequest;
import com.umc.todayter.domain.place.dto.response.ExploreFiltersResponse;
import com.umc.todayter.domain.place.dto.response.PlaceListResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchResponse;
import com.umc.todayter.domain.place.service.PlaceSearchService;
import com.umc.todayter.domain.place.service.PlaceService;
import com.umc.todayter.domain.place.service.PlaceThumbnailService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Place", description = "장소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceSearchService placeSearchService;
    private final PlaceThumbnailService placeThumbnailService;

    @Operation(summary = "장소 검색 목록 조회", description = "검색어, 지역, 테마, 오행 조건으로 활성 장소 목록을 조회합니다.")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<ApiResponse<PlaceSearchResponse>> searchPlaces(
            @Valid @ModelAttribute PlaceSearchRequest request
    ) {
        String contextPathUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .toUriString();
        PlaceSearchResponse result = placeSearchService.searchPlaces(request, contextPathUrl);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "탐색 필터 조회", description = "지역, 테마, 오행 탐색 필터를 조회합니다.")
    @SecurityRequirements
    @GetMapping("/explore-filters")
    public ResponseEntity<ApiResponse<ExploreFiltersResponse>> getExploreFilters() {
        ExploreFiltersResponse result = placeService.getExploreFilters();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "저장한 터/다녀온 터 목록 조회", description = "type=saved(저장한 터) 또는 type=visited(다녀온 터) 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PlaceListResponse>> getMyPlaces(@RequestParam(required = false) String type) {
        PlaceListResponse result = placeService.getMyPlaces(type);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "장소 대표 이미지 조회", description = "Google Places 기반 장소 대표 이미지 연동")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Redirect to representative image URI"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid placeId format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Active place, Google Place ID, or representative photo not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Google Places integration failure")
    })
    @SecurityRequirements
    @GetMapping("/{placeId}/thumbnail")
    public ResponseEntity<Void> getThumbnail(@PathVariable Long placeId) {
        URI photoUri = placeThumbnailService.getThumbnailUri(placeId);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(photoUri)
                .cacheControl(CacheControl.noStore())
                .build();
    }
}
