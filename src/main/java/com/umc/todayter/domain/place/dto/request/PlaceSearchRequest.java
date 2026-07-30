package com.umc.todayter.domain.place.dto.request;

import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PlaceSearchRequest {

    private String keyword;
    private RegionCode regionCode;
    private ThemeType themeType;
    private ElementType elementType;

    @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "latitude는 90 이하여야 합니다.")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "longitude는 180 이하여야 합니다.")
    private Double longitude;

    @NotNull(message = "page는 필수입니다.")
    @Min(value = 0, message = "page는 0 이상이어야 합니다.")
    private Integer page = 0;

    @NotNull(message = "size는 필수입니다.")
    @Min(value = 1, message = "size는 1 이상이어야 합니다.")
    @Max(value = 50, message = "size는 50 이하여야 합니다.")
    private Integer size = 20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public RegionCode getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(RegionCode regionCode) {
        this.regionCode = regionCode;
    }

    public ThemeType getThemeType() {
        return themeType;
    }

    public void setThemeType(ThemeType themeType) {
        this.themeType = themeType;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getTrimmedKeyword() {
        if (keyword == null) {
            return null;
        }
        return keyword.trim();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    @AssertTrue(message = "keyword는 1자 이상 50자 이하여야 합니다.")
    @Schema(hidden = true)
    public boolean isKeywordValid() {
        String trimmedKeyword = getTrimmedKeyword();
        return trimmedKeyword == null || (!trimmedKeyword.isBlank() && trimmedKeyword.length() <= 50);
    }

    @AssertTrue(message = "latitude와 longitude는 함께 전달해야 합니다.")
    @Schema(hidden = true)
    public boolean isCoordinatePairValid() {
        return (latitude == null && longitude == null) || (latitude != null && longitude != null);
    }
}
