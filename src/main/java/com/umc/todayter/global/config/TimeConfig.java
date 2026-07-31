package com.umc.todayter.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock seoulClock() {
        return Clock.system(SEOUL_ZONE_ID);
    }
}
