package com.umc.todayter.domain.place.repository;

import com.umc.todayter.domain.place.entity.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long>, JpaSpecificationExecutor<Place> {

    @Query("""
            select p.themeType as themeType, count(p) as placeCount
            from Place p
            where p.active = true
            group by p.themeType
            """)
    List<ThemePlaceCount> countActivePlacesGroupByThemeType();

    Optional<Place> findByIdAndActiveTrue(Long id);
    Optional<Place> findByName(String name);

    List<Place> findByActiveTrueAndEditorPickTrue(Pageable pageable);
}
