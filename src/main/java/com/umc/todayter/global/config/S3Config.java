package com.umc.todayter.global.config;

import com.umc.todayter.global.config.properties.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    // 자격증명 검증을 클라이언트 생성 시점이 아니라 실제 요청 시점으로 미룸
    // -> AWS 자격증명이 아직 없는 로컬/CI 환경에서도 앱 기동과 컨텍스트 로드 테스트가 깨지지 않음
    @Bean
    public AwsCredentialsProvider s3CredentialsProvider(S3Properties properties) {
        return () -> AwsBasicCredentials.create(properties.accessKey(), properties.secretKey());
    }

    @Bean
    public S3Client s3Client(S3Properties properties, AwsCredentialsProvider s3CredentialsProvider) {
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(s3CredentialsProvider)
                .build();
    }
}
