package com.roadmap.backend.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /** 관리자 토큰 유효기간: 100년 (사실상 무제한) */
    private static final long DEFAULT_EXPIRATION_MS = 100L * 365 * 24 * 60 * 60 * 1000;

    private String secret;
    private long expirationMs = DEFAULT_EXPIRATION_MS;
}
