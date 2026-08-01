package com.umc.todayter.domain.fortune.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum FiveElement {
    WOOD("목"),
    FIRE("화"),
    EARTH("토"),
    METAL("금"),
    WATER("수");

    private final String label;

    public static FiveElement fromLabel(String label) {
        if (label == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(element -> element.label.equals(label.trim()))
                .findFirst()
                .orElse(null);
    }
}
