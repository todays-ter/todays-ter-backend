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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return places;
            }
            Map<String, Integer> header = toHeaderIndex(splitCsvLine(headerLine));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                places.add(toPlace(header, splitCsvLine(line)));
            }
        }

        return places;
    }

    private Map<String, Integer> toHeaderIndex(String[] header) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            index.put(header[i], i);
        }
        return index;
    }

    private String[] splitCsvLine(String line) {
        String[] fields = CSV_SPLIT_PATTERN.split(line, -1);
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            if (i == 0 && field.startsWith("\uFEFF")) {
                field = field.substring(1);
            }
            if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
                field = field.substring(1, field.length() - 1).replace("\"\"", "\"");
            }
            fields[i] = field;
        }
        return fields;
    }

    private Place toPlace(Map<String, Integer> header, String[] f) {
        return Place.builder()
                .name(field(header, f, "name"))
                .address(field(header, f, "address"))
                .latitude(Double.parseDouble(field(header, f, "latitude")))
                .longitude(Double.parseDouble(field(header, f, "longitude")))
                .elementType(ElementType.valueOf(field(header, f, "elementType")))
                .terrainType(field(header, f, "terrainType"))
                .loveScore(Integer.parseInt(field(header, f, "loveScore")))
                .relationshipScore(Integer.parseInt(field(header, f, "relationshipScore")))
                .careerScore(Integer.parseInt(field(header, f, "careerScore")))
                .studyScore(Integer.parseInt(field(header, f, "studyScore")))
                .restScore(Integer.parseInt(field(header, f, "restScore")))
                .transitionScore(Integer.parseInt(field(header, f, "transitionScore")))
                .themeType(ThemeType.valueOf(field(header, f, "themeType")))
                .summary(field(header, f, "summary"))
                .description(field(header, f, "description"))
                .editorPick(Boolean.parseBoolean(field(header, f, "editorPick")))
                .active(Boolean.parseBoolean(field(header, f, "active")))
                .mapUrl(optionalField(header, f, "mapUrl"))
                .googlePlaceId(optionalField(header, f, "googlePlaceId"))
                .regionCode(RegionCode.valueOf(field(header, f, "regionCode")))
                .averageRating(0.0)
                .reviewCount(0)
                .build();
    }

    private String field(Map<String, Integer> header, String[] fields, String name) {
        Integer index = header.get(name);
        if (index == null || index >= fields.length || fields[index].isBlank()) {
            throw new IllegalArgumentException("places.csv 필수 컬럼 값이 비어 있습니다: " + name);
        }
        return fields[index];
    }

    private String optionalField(Map<String, Integer> header, String[] fields, String name) {
        Integer index = header.get(name);
        if (index == null || index >= fields.length || fields[index].isBlank()) {
            return null;
        }
        return fields[index];
    }
}
