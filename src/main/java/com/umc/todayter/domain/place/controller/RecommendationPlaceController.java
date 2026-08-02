package com.umc.todayter.domain.place.controller;

import com.umc.todayter.domain.place.dto.response.PlaceDetailResponse;
import com.umc.todayter.domain.place.dto.request.PlaceBookmarkRequest;
import com.umc.todayter.domain.place.dto.response.PlaceBookmarkResponse;
import com.umc.todayter.domain.place.service.PlaceService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.dto.response.ShareLinkResponse;
import com.umc.todayter.global.service.ShareUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Recommendation Place", description = "홈 추천 장소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations/places")
public class RecommendationPlaceController {

    private final PlaceService placeService;
    private final ShareUrlService shareUrlService;

    @Operation(summary = "추천 장소 상세 조회", description = "홈에서 추천한 장소의 상세 정보를 조회합니다.")
    @SecurityRequirements
    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getRecommendedPlaceDetail(
            @PathVariable Long placeId
    ) {
        String contextPathUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        PlaceDetailResponse result = placeService.getRecommendedPlaceDetail(placeId, contextPathUrl);
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "추천 장소 공유 링크 생성", description = "추천 장소 상세 페이지로 이동하는 공유 링크를 생성합니다.")
    @SecurityRequirements
    @PostMapping("/{placeId}/share")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @PathVariable Long placeId
    ) {
        placeService.validateActivePlace(placeId);
        ShareLinkResponse result = ShareLinkResponse.forPlace(shareUrlService.recommendedPlaceUrl(placeId));
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(summary = "추천 장소 북마크 변경", description = "isSaved=true이면 장소를 저장하고 false이면 저장을 해제합니다.")
    @PatchMapping("/{placeId}/bookmark")
    public ResponseEntity<ApiResponse<PlaceBookmarkResponse>> updateBookmark(
            @PathVariable Long placeId,
            @Valid @RequestBody PlaceBookmarkRequest request
    ) {
        PlaceBookmarkResponse result = placeService.updateBookmark(placeId, request.isSaved());
        return ResponseEntity.ok(ApiResponse.onSuccess(result, SuccessCode.OK));
    }
}
