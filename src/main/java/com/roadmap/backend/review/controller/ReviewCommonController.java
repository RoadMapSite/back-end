package com.roadmap.backend.review.controller;

import com.roadmap.backend.review.dto.ReviewResponse;
import com.roadmap.backend.review.dto.ReviewsResponse;
import com.roadmap.backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/common/reviews")
@Tag(name = "Review", description = "후기 관련 API")
@RequiredArgsConstructor
public class ReviewCommonController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(
            summary = "전체 후기 목록 조회",
            description = """
                    전체 후기 목록을 조회합니다.
                    """
    )
    public ResponseEntity<ReviewsResponse> getReviews(
            @Parameter(description = "페이지 번호", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "관", required = false)
            @RequestParam String branch
    ) {
        ReviewsResponse response = reviewService.getReviews(page, branch);
        return ResponseEntity.ok(response);
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
