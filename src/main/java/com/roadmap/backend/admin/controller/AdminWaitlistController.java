package com.roadmap.backend.admin.controller;

import com.roadmap.backend.admin.dto.AdminWaitlistResponse;
import com.roadmap.backend.admin.dto.WaitlistStatusUpdateRequest;
import com.roadmap.backend.admin.dto.WaitlistStatusUpdateResponse;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.admin.service.AdminWaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/waitlists")
@Tag(name = "Admin", description = "관리자 인증 및 관리 API")
@RequiredArgsConstructor
public class AdminWaitlistController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AdminWaitlistService adminWaitlistService;

    @GetMapping
    @Operation(
            summary = "등록 대기 학생 리스트 조회",
            description = """
                    관리자 전용. 시즌별 대기열 목록을 조회합니다.
                    registeredAt 오름차순 정렬, 대기 순번은 1번부터 순서대로 부여됩니다.
                    
                    **필수**: season
                    **선택**: branch (SEMESTER_1, SEMESTER_2 시즌일 때 필수)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminWaitlistResponse> getWaitlistList(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "시즌 (SUMMER, WINTER, SEMESTER_1, SEMESTER_2)", required = true)
            @RequestParam(name = "season", required = false) String season,
            @Parameter(description = "지점 (N 또는 Hi-end, 학기 시즌일 때 필수)")
            @RequestParam(name = "branch", required = false) String branch) {

        String token = extractToken(authHeader);
        requireToken(token);
        AdminWaitlistResponse response = adminWaitlistService.getWaitlistList(token, season, branch);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{waitlistId}/status")
    @Operation(
            summary = "등록 대기 학생 상태 변경",
            description = "관리자 전용. 특정 학생의 대기 상태를 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<WaitlistStatusUpdateResponse> updateWaitlistStatus(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(name = "waitlistId", in = ParameterIn.PATH, required = true, description = "대기열 ID",
                    schema = @Schema(type = "integer", example = "1"))
            @PathVariable("waitlistId") Long waitlistId,
            @Valid @RequestBody WaitlistStatusUpdateRequest request) {

        String token = extractToken(authHeader);
        requireToken(token);
        WaitlistStatusUpdateResponse response = adminWaitlistService.updateWaitlistStatus(
                token, waitlistId, request);
        return ResponseEntity.ok(response);
    }

    private void requireToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AdminAuthException("토큰이 없습니다.", HttpStatus.UNAUTHORIZED);
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
