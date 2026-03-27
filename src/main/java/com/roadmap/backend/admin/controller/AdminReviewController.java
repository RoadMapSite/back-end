package com.roadmap.backend.admin.controller;

import com.roadmap.backend.admin.dto.AdminReviewDetailResponse;
import com.roadmap.backend.admin.dto.AdminReviewModels.PageResponse;
import com.roadmap.backend.admin.dto.ReviewStatusUpdateRequest;
import com.roadmap.backend.admin.dto.ReviewStatusUpdateResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/reviews")
@Tag(name = "Admin", description = "관리자 인증 및 관리 API")
@RequiredArgsConstructor
public class AdminReviewController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AdminReviewService adminReviewService;

    @GetMapping
    @Operation(
            summary = "전체 후기 조회",
            description = """
                    관리자 전용. 승인 여부와 관계없이 등록된 모든 후기를 생성일 최신순으로 페이징 조회합니다.
                    작성자명은 마스킹 없이 실명으로 반환됩니다.
                    
                    - **page**: 1부터 시작 (Spring 내부에서는 0-based로 변환)
                    - **size**: 페이지 크기 (기본 10)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PageResponse> getAllReviews(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "페이지 번호 (1부터)", example = "1")
            @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size) {

        String token = extractToken(authHeader);
        requireToken(token);
        int safePage = page < 1 ? 1 : page;
        int safeSize = size < 1 ? 10 : size;
        PageResponse response = adminReviewService.getAllReviews(token, safePage, safeSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}")
    @Operation(
            summary = "상세 후기 조회",
            description = """
                    관리자 전용. 특정 후기의 전체 내용을 조회합니다.
                    일반 사용자용 상세와 달리 **조회수(viewCount)는 증가하지 않습니다.**
                    작성자명은 마스킹 없이 실명으로 반환됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminReviewDetailResponse> getReviewDetail(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(name = "reviewId", in = ParameterIn.PATH, required = true, description = "후기 ID",
                    schema = @Schema(type = "integer", example = "14"))
            @PathVariable("reviewId") Long reviewId) {

        String token = extractToken(authHeader);
        requireToken(token);
        AdminReviewDetailResponse response = adminReviewService.getReviewDetail(token, reviewId);
        return ResponseEntity.ok(response);
    }

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

    @PatchMapping("/{reviewId}/status")
    @Operation(
            summary = "후기 승인 여부 결정",
            description = "관리자 전용. 특정 후기의 승인 상태를 변경합니다. (PENDING: 대기, APPROVED: 승인)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReviewStatusUpdateResponse> updateReviewStatus(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(name = "reviewId", in = ParameterIn.PATH, required = true, description = "후기 ID",
                    schema = @Schema(type = "integer", example = "1"))
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReviewStatusUpdateRequest request) {

        String token = extractToken(authHeader);
        requireToken(token);
        ReviewStatusUpdateResponse response = adminReviewService.updateReviewStatus(token, reviewId, request);
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
