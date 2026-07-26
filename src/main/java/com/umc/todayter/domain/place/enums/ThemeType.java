package com.umc.todayter.domain.place.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ThemeType {
    LOVE("연애 터", 1),
    CAREER("커리어 터", 2),
    WEALTH("재물 터", 3),
    RELATIONSHIP("인간관계 터", 4),
    HEALTH("건강 터", 5),
    ETC("기타", 6);

    private final String displayName;
    private final int displayOrder;
}
