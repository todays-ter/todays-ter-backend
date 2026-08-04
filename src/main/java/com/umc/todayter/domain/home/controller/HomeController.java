package com.umc.todayter.domain.home.controller;

import com.umc.todayter.domain.home.dto.request.HomeRecommendedPlaceQuery;
import com.umc.todayter.domain.home.dto.response.HomeHeaderResponse;
import com.umc.todayter.domain.home.dto.response.HomeRecommendedPlacesResponse;
import com.umc.todayter.domain.home.dto.response.TodayEnergyResponse;
import com.umc.todayter.domain.home.dto.response.EnergyRoutinesResponse;
import com.umc.todayter.domain.home.service.EnergyRoutineService;
import com.umc.todayter.domain.home.service.HomeService;
import com.umc.todayter.domain.home.service.RecommendedPlaceService;
import com.umc.todayter.domain.home.service.TodayEnergyService;
import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserContextResolver;
import com.umc.todayter.global.util.GuestCookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Home", description = "홈 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final CurrentUserContextResolver currentUserContextResolver;
    private final HomeService homeService;
    private final TodayEnergyService todayEnergyService;
    private final EnergyRoutineService energyRoutineService;
    private final RecommendedPlaceService recommendedPlaceService;

    @Operation(
            summary = "홈 인사 헤더 조회",
            description = """
                    Bearer JWT 또는 guest_id 쿠키가 필요합니다.
                    둘 다 있으면 JWT 회원을 우선합니다.
                    유효한 인증 수단이 없으면 401을 반환합니다.
                    """
    )
    @GetMapping("/header")
    public ResponseEntity<ApiResponse<HomeHeaderResponse>> getHeader(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId
    ) {
        CurrentUserContext context = currentUserContextResolver.resolve(guestId);
        HomeHeaderResponse result = homeService.getHeader(context);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(
            summary = "오늘 나의 기운 조회",
            description = """
                    Bearer JWT 또는 guest_id 쿠키가 필요합니다.
                    둘 다 있으면 JWT 회원을 우선합니다.
                    사용자의 최신 완료 사주 리포트에서 대표 오행과 설명을 반환합니다.
                    완료된 리포트가 없으면 404를 반환합니다.
                    """
    )
    @GetMapping("/today-energy")
    public ResponseEntity<ApiResponse<TodayEnergyResponse>> getTodayEnergy(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId
    ) {
        CurrentUserContext context = currentUserContextResolver.resolve(guestId);
        TodayEnergyResponse result = todayEnergyService.getTodayEnergy(context);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(
            summary = "홈 에너지 루틴 조회",
            description = """
                    Bearer JWT 또는 guest_id 쿠키가 필요합니다.
                    둘 다 있으면 JWT 회원을 우선합니다.
                    최신 완료 사주 리포트의 보완 오행을 기준으로
                    최대 3개의 에너지 루틴을 반환합니다.
                    """
    )
    @GetMapping("/energy-routines")
    public ResponseEntity<ApiResponse<EnergyRoutinesResponse>> getEnergyRoutines(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId
    ) {
        CurrentUserContext context = currentUserContextResolver.resolve(guestId);
        EnergyRoutinesResponse result = energyRoutineService.getEnergyRoutines(context);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }

    @Operation(
            summary = "오늘 가장 잘 맞는 터 조회",
            description = """
                    Bearer JWT 또는 guest_id 쿠키가 필요합니다.
                    둘 다 있으면 JWT 회원을 우선합니다.
                    회원 또는 비회원의 최신 완료 사주 리포트를 기준으로
                    활성 장소의 추천 점수를 계산합니다.
                    비회원은 상위 1개, 회원은 상위 3개를 반환합니다.
                    latitude와 longitude를 함께 전달하면 거리를 반환합니다.
                    """
    )
    @GetMapping("/recommended-place")
    public ResponseEntity<ApiResponse<HomeRecommendedPlacesResponse>> getRecommendedPlace(
            @CookieValue(
                    name = GuestCookieUtil.COOKIE_NAME,
                    required = false
            ) String guestId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude
    ) {
        HomeRecommendedPlaceQuery query = HomeRecommendedPlaceQuery.of(latitude, longitude);
        CurrentUserContext context = currentUserContextResolver.resolve(guestId);
        String contextPathUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        HomeRecommendedPlacesResponse result = recommendedPlaceService.getRecommendedPlaces(
                context,
                query,
                contextPathUrl
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(result, SuccessCode.OK));
    }
}
