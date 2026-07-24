package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("""
            select p.themeType as themeType, count(p) as placeCount
            from Place p
            where p.active = true
            group by p.themeType
            """)
    List<ThemePlaceCount> countActivePlacesGroupByThemeType();
}
