package com.umc.todayter.domain.place.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ElementType {
    FIRE("화", 1),
    EARTH("토", 2),
    WOOD("목", 3),
    WATER("수", 4),
    METAL("금", 5);

    private final String displayName;
    private final int displayOrder;
}
