package com.umc.todayter.global.config;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceSeeder implements ApplicationRunner {

    private static final String SEED_CSV_PATH = "seed/places.csv";
    private static final Pattern CSV_SPLIT_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private final PlaceRepository placeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        List<Place> places = readSeedPlaces();
        if (placeRepository.count() > 0) {
            syncExistingPlaces(places);
            return;
        }

        try {
            placeRepository.saveAll(places);
        } catch (DataIntegrityViolationException e) {
            // 여러 인스턴스가 동시에 기동해 count() == 0을 함께 통과한 경우 발생.
            // name unique 제약이 이 경합을 막아주므로, 먼저 커밋한 인스턴스만 시딩에 성공하고
            // 나머지는 여기서 조용히 스킵한다 (앱 기동 자체를 실패시키지 않음).
            log.warn("장소 초기 데이터가 다른 인스턴스에 의해 이미 시딩되어 스킵합니다.", e);
        }
    }

    private void syncExistingPlaces(List<Place> seedPlaces) {
        seedPlaces.stream()
                .forEach(seedPlace -> placeRepository.findByName(seedPlace.getName())
                        .ifPresent(existingPlace -> {
                            if (StringUtils.hasText(seedPlace.getMapUrl())) {
                                existingPlace.updateMapUrl(seedPlace.getMapUrl());
                            }
                            if (StringUtils.hasText(seedPlace.getGooglePlaceId())) {
                                existingPlace.updateGooglePlaceId(seedPlace.getGooglePlaceId());
                            }
                        }));
    }

    private List<Place> readSeedPlaces() throws IOException {
        List<Place> places = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(SEED_CSV_PATH).getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                return places;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                places.add(toPlace(splitCsvLine(line)));
            }
        }

        return places;
    }

    private String[] splitCsvLine(String line) {
        String[] fields = CSV_SPLIT_PATTERN.split(line, -1);
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
                field = field.substring(1, field.length() - 1).replace("\"\"", "\"");
            }
            fields[i] = field;
        }
        return fields;
    }

    private Place toPlace(String[] f) {
        // name,address,latitude,longitude,elementType,terrainType,
        // loveScore,relationshipScore,careerScore,studyScore,restScore,transitionScore,
        // themeType,summary,description,editorPick,active,mapUrl,googlePlaceId
        return Place.builder()
                .name(f[0])
                .address(f[1])
                .latitude(Double.parseDouble(f[2]))
                .longitude(Double.parseDouble(f[3]))
                .elementType(ElementType.valueOf(f[4]))
                .terrainType(f[5])
                .loveScore(Integer.parseInt(f[6]))
                .relationshipScore(Integer.parseInt(f[7]))
                .careerScore(Integer.parseInt(f[8]))
                .studyScore(Integer.parseInt(f[9]))
                .restScore(Integer.parseInt(f[10]))
                .transitionScore(Integer.parseInt(f[11]))
                .themeType(ThemeType.valueOf(f[12]))
                .summary(f[13])
                .description(f[14])
                .editorPick(Boolean.parseBoolean(f[15]))
                .active(Boolean.parseBoolean(f[16]))
                .mapUrl(f.length > 17 && !f[17].isBlank() ? f[17] : null)
                .googlePlaceId(f.length > 18 && !f[18].isBlank() ? f[18] : null)
                .regionCode(RegionCode.SEOUL)
                .averageRating(0.0)
                .reviewCount(0)
                .build();
    }
}
