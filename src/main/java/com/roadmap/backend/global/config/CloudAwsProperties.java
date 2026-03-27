package com.roadmap.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * cloud.aws.* 바인딩 (application.yml / 환경 변수).
 */
@Getter
@ConfigurationProperties(prefix = "cloud.aws")
public class CloudAwsProperties {

    private final Credentials credentials = new Credentials();
    private final RegionConfig region = new RegionConfig();
    private final S3 s3 = new S3();

    @Getter
    @Setter
    public static class Credentials {
        /** maps to access-key */
        private String accessKey;
        /** maps to secret-key */
        private String secretKey;
    }

    /**
     * region.static YAML 바인딩용 (Java 예약어 static 대신 setStatic 사용).
     */
    public static class RegionConfig {

        private String staticValue;

        public void setStatic(String value) {
            this.staticValue = value;
        }

        public String getStatic() {
            return staticValue;
        }
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
    }
}
