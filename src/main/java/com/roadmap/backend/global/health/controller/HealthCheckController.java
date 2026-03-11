package com.roadmap.backend.global.health.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class HealthCheckController {

    @GetMapping("/api/ping")
    @Operation(summary = "health check")
    public String ping() {
        return "pong";
    }
}
