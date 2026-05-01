package com.roadmap.backend.review.controller;

import com.roadmap.backend.review.dto.ReviewResponse;
import com.roadmap.backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/common/reviews")
@Tag(name = "Review", description = "후기 관련 API")
@RequiredArgsConstructor
public class ReviewCommonController {

    private final ReviewService reviewService;

    @GetMapping("/top")
    @Operation(
            summary = "우수 후기 목록 조회",
            description = """
                    승인되었고 우수(isTop=true)로 지정된 후기만 조회합니다.
                    페이징 없음, 최신순(작성일 내림차순)입니다.
                    목록 응답의 작성자명은 마스킹 처리됩니다.
                    """
    )
    public ResponseEntity<List<ReviewResponse>> getTopReviews() {
        return ResponseEntity.ok(reviewService.getTopApprovedReviews());
    }

    @GetMapping
    @Operation(
            summary = "일반 후기 목록 조회 (페이징)",
            description = """
                    승인된 후기 중 우수 후기가 아닌 것(isTop=false)만 페이징 조회합니다.
                    정렬: 작성일 최신순(createdAt DESC).
                    Spring Page 응답으로 totalElements 등 페이징 메타데이터가 포함됩니다.
                    목록 응답의 작성자명은 마스킹 처리됩니다.
                    page는 0부터 시작합니다.
                    """
    )
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @ParameterObject
            @PageableDefault(size = 10)
            Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getApprovedRegularReviews(pageable));
    }

    @GetMapping("/{reviewId}")
    @Operation(
            summary = "개별 후기 상세 조회",
            description = """
                    개별 후기 상세 정보를 조회합니다.
                    """
    )
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(name = "reviewId", in = ParameterIn.PATH, required = true, description = "후기 ID",
                    schema = @Schema(type = "long", example = "1"))
            @PathVariable Long reviewId
    ) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }
}
