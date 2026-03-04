package com.roadmap.backend.global.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health-Check-Controller", description = "서버 관리 API")
public class HealthCheckController {

    @GetMapping("/api/ping")
    @Operation(summary = "health check")
    public String ping() {
        return "pong";
    }
}
