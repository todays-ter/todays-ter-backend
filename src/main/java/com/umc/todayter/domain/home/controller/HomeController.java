package com.umc.todayter.domain.home.controller;

import com.umc.todayter.domain.home.dto.response.HomeHeaderResponse;
import com.umc.todayter.domain.home.dto.response.TodayEnergyResponse;
import com.umc.todayter.domain.home.service.HomeService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final CurrentUserContextResolver currentUserContextResolver;
    private final HomeService homeService;
    private final TodayEnergyService todayEnergyService;

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
}
