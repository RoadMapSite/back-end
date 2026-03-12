package com.roadmap.backend.admin.controller;

import com.roadmap.backend.admin.dto.ReviewTopUpdateRequest;
import com.roadmap.backend.admin.dto.ReviewTopUpdateResponse;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.admin.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/reviews")
@Tag(name = "Admin", description = "관리자 인증 및 관리 API")
@RequiredArgsConstructor
public class AdminReviewController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AdminReviewService adminReviewService;

    @PatchMapping("/{reviewId}/top")
    @Operation(
            summary = "우수 후기 지정 및 해제",
            description = "관리자 전용. 특정 후기를 우수 후기로 지정하거나 해제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReviewTopUpdateResponse> updateReviewTop(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(name = "reviewId", in = ParameterIn.PATH, required = true, description = "후기 ID",
                    schema = @Schema(type = "integer", example = "1"))
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReviewTopUpdateRequest request) {

        String token = extractToken(authHeader);
        requireToken(token);
        ReviewTopUpdateResponse response = adminReviewService.updateReviewTop(token, reviewId, request);
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
