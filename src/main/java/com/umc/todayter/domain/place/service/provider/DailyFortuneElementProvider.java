package com.umc.todayter.domain.place.service.provider;

import com.umc.todayter.domain.place.enums.ElementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailyFortuneElementProvider {

    private static final LocalDate JIA_ZI_REFERENCE_DATE = LocalDate.of(2000, 1, 7);

    private final Clock clock;

    public ElementType todayElement() {
        return elementOf(LocalDate.now(clock));
    }

    ElementType elementOf(LocalDate date) {
        long cycleIndex = Math.floorMod(
                date.toEpochDay() - JIA_ZI_REFERENCE_DATE.toEpochDay(),
                60
        );
        int heavenlyStemIndex = (int) (cycleIndex % 10);

        return switch (heavenlyStemIndex) {
            case 0, 1 -> ElementType.WOOD;
            case 2, 3 -> ElementType.FIRE;
            case 4, 5 -> ElementType.EARTH;
            case 6, 7 -> ElementType.METAL;
            case 8, 9 -> ElementType.WATER;
            default -> throw new IllegalStateException("일진 오행을 계산할 수 없습니다.");
        };
    }
}
