package com.umc.todayter.global.config;

import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PlaceSeeder implements ApplicationRunner {

    private static final String SEED_CSV_PATH = "seed/places.csv";
    private static final Pattern CSV_SPLIT_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private final PlaceRepository placeRepository;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (placeRepository.count() > 0) {
            return;
        }

        List<Place> places = readSeedPlaces();
        placeRepository.saveAll(places);
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
        // themeType,summary,description,editorPick,active
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
                .regionCode(RegionCode.SEOUL)
                .averageRating(0.0)
                .reviewCount(0)
                .build();
    }
}
