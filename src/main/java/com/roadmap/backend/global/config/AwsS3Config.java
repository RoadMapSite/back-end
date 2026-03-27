package com.roadmap.backend.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(CloudAwsProperties.class)
public class AwsS3Config {

    @Bean
    @ConditionalOnProperty(prefix = "cloud.aws.s3", name = "bucket")
    public S3Client s3Client(CloudAwsProperties cloudAwsProperties) {
        String bucket = cloudAwsProperties.getS3().getBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("cloud.aws.s3.bucket 값이 비어 있습니다.");
        }
        CloudAwsProperties.Credentials creds = cloudAwsProperties.getCredentials();
        String accessKey = creds.getAccessKey();
        String secretKey = creds.getSecretKey();
        if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
            throw new IllegalStateException(
                    "AWS 자격 증명이 설정되지 않았습니다. cloud.aws.credentials.access-key / secret-key (또는 환경 변수 AWS_ACCESS_KEY, AWS_SECRET_KEY)를 설정하세요.");
        }
        String regionStr = cloudAwsProperties.getRegion().getStatic();
        if (!StringUtils.hasText(regionStr)) {
            regionStr = "ap-northeast-2";
        }
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey.trim(), secretKey.trim());
        return S3Client.builder()
                .region(Region.of(regionStr.trim()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}

