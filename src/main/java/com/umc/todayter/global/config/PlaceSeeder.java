package com.umc.todayter.global.config;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

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

    private final PlaceSeedTransactionService placeSeedTransactionService;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        readSeedPlaces().forEach(this::seedPlace);
    }

    private void seedPlace(Place seedPlace) {
        try {
            placeSeedTransactionService.seedPlace(seedPlace);
        } catch (PlaceSeedDataIntegrityException e) {
            if (e.isInsert() && placeSeedTransactionService.existsByName(seedPlace.getName())) {
                log.warn("Place seed was already inserted by another instance. name={}", seedPlace.getName(), e);
                return;
            }
            throw e.getDataIntegrityViolationException();
        }
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
            throw new IllegalArgumentException("places.csv required column is blank: " + name);
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
