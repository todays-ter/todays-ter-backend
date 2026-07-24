package com.umc.todayter.domain.place.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegionCode {
    SEOUL("서울", 1),
    JEJU("제주", 2),
    BUSAN("부산", 3),
    GANGWON("강원", 4),
    CAPITAL_AREA("수도권", 5);

    private final String displayName;
    private final int displayOrder;
}
