package com.roadmap.backend.admin.controller;

import com.roadmap.backend.admin.dto.AdminConsultationListResponse;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.admin.service.AdminConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/consultations")
@Tag(name = "Admin", description = "관리자 인증 및 관리 API")
@RequiredArgsConstructor
public class AdminConsultationController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AdminConsultationService adminConsultationService;

    @GetMapping
    @Operation(
            summary = "상담 신청 리스트 조회",
            description = """
                    관리자 전용. 프론트엔드 달력 렌더링에 최적화된 상담 신청 목록을 조회합니다.
                    날짜·시간 오름차순 정렬로 반환됩니다.
                    
                    **필수 파라미터**: branch, startDate, endDate (누락 시 400)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminConsultationListResponse> getConsultationList(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "지점 (N 또는 Hi-end)", required = true)
            @RequestParam(name = "branch", required = false) String branch,
            @Parameter(description = "달력 시작일 (yyyy-MM-dd)", required = true)
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "달력 종료일 (yyyy-MM-dd)", required = true)
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String token = extractToken(authHeader);
        requireToken(token);
        validateRequiredParams(branch, startDate, endDate);

        AdminConsultationListResponse response = adminConsultationService.getConsultationList(
                token, branch, startDate, endDate);
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

    private void validateRequiredParams(String branch, LocalDate startDate, LocalDate endDate) {
        if (branch == null || branch.isBlank()) {
            throw new AdminAuthException("branch는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (startDate == null) {
            throw new AdminAuthException("startDate는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (endDate == null) {
            throw new AdminAuthException("endDate는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (startDate.isAfter(endDate)) {
            throw new AdminAuthException("startDate는 endDate 이전이어야 합니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
