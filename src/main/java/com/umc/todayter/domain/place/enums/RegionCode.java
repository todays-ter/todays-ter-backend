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

    public static RegionCode fromSeedValue(String value) {
        for (RegionCode regionCode : values()) {
            if (regionCode.name().equals(value) || regionCode.displayName.equals(value)) {
                return regionCode;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 지역 코드입니다: " + value);
    }
}
