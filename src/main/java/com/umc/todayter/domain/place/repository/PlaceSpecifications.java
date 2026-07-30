package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class PlaceSpecifications {

    private static final char LIKE_ESCAPE_CHAR = '\\';

    private PlaceSpecifications() {
    }

    public static Specification<Place> active() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("active"));
    }

    public static Specification<Place> keywordContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + escapeLikePattern(keyword.toLowerCase(Locale.ROOT)) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern, LIKE_ESCAPE_CHAR),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), likePattern, LIKE_ESCAPE_CHAR),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), likePattern, LIKE_ESCAPE_CHAR)
            );
        };
    }

    private static String escapeLikePattern(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static Specification<Place> regionCodeEquals(RegionCode regionCode) {
        return (root, query, criteriaBuilder) -> regionCode == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("regionCode"), regionCode);
    }

    public static Specification<Place> themeTypeEquals(ThemeType themeType) {
        return (root, query, criteriaBuilder) -> themeType == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("themeType"), themeType);
    }

    public static Specification<Place> elementTypeEquals(ElementType elementType) {
        return (root, query, criteriaBuilder) -> elementType == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("elementType"), elementType);
    }
}
