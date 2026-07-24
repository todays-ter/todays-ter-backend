package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.enums.ThemeType;

public interface ThemePlaceCount {
    ThemeType getThemeType();

    long getPlaceCount();
}
