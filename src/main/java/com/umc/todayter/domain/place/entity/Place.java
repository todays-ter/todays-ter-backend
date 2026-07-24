package com.umc.todayter.domain.place.entity;

import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "places")
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at", nullable = false))
})
public class Place extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_code", nullable = false)
    private RegionCode regionCode;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", nullable = false)
    private ElementType elementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_type", nullable = false)
    private ThemeType themeType;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(name = "is_editor_pick", nullable = false)
    private Boolean editorPick;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    private Place(
            String name,
            String summary,
            String description,
            String address,
            RegionCode regionCode,
            Double latitude,
            Double longitude,
            ElementType elementType,
            ThemeType themeType,
            Double averageRating,
            Integer reviewCount,
            Boolean editorPick,
            Boolean active
    ) {
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.address = address;
        this.regionCode = regionCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elementType = elementType;
        this.themeType = themeType;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.editorPick = editorPick;
        this.active = active;
    }

    public static Place create(
            String name,
            String summary,
            String description,
            String address,
            RegionCode regionCode,
            Double latitude,
            Double longitude,
            ElementType elementType,
            ThemeType themeType,
            Double averageRating,
            Integer reviewCount,
            Boolean editorPick,
            Boolean active
    ) {
        return new Place(
                name,
                summary,
                description,
                address,
                regionCode,
                latitude,
                longitude,
                elementType,
                themeType,
                averageRating,
                reviewCount,
                editorPick,
                active
        );
    }
}
