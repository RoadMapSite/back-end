package com.roadmap.backend.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RoadMap API 명세서")
                        .version("v1.0.0")
                        .description("입시 관리형 독서실 RoadMap 백엔드 API 공식 문서입니다!"))
                .servers(createServerList());
    }

    private List<Server> createServerList() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Server")
                // 향후 운영 서버 추가 예시:
                // new Server().url("https://api.roadmap.example.com").description("Production Server")
        );
    }
}
