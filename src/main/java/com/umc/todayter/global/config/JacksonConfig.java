package com.umc.todayter.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spring Boot의 기본 JSON 엔진은 Jackson 3(tools.jackson)이라 com.fasterxml.jackson 기반
// ObjectMapper 빈이 자동 등록되지 않는다. 옛 Jackson 2 API(JsonNode 등)를 쓰는 곳을 위해 별도로 등록한다.
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
