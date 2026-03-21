package com.roadmap.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RoadMap API 명세서")
                        .version("v1.0.0")
                        .description("입시 관리형 독서실 RoadMap 백엔드 API 공식 문서입니다!"))
                .servers(createServerList())
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("휴대폰 인증 후 발급받은 verificationToken을 입력하세요")));
    }

    private List<Server> createServerList() {
        return List.of(
                new Server()
                        .url("http://3.225.101.84:8080")
                        .description("운영 서버"),
                new Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버")
        );
    }
}
