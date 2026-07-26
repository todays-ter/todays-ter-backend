package com.umc.todayter.domain.fortune.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class FortuneReportClientConfig {

    @Bean
    @Qualifier("sazuRestClient")
    public RestClient sazuRestClient(SazuApiProperties properties) {
        return createClient(properties.baseUrl(), properties.connectTimeout(), properties.readTimeout());
    }

    @Bean
    @Qualifier("openAiReportRestClient")
    public RestClient openAiReportRestClient(OpenAiReportProperties properties) {
        return createClient(properties.baseUrl(), properties.connectTimeout(), properties.readTimeout());
    }

    private RestClient createClient(String baseUrl, java.time.Duration connectTimeout, java.time.Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
