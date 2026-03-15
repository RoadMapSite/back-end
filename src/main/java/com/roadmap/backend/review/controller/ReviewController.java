package com.roadmap.backend.review.controller;

import com.roadmap.backend.review.dto.MyReviewsResponse;
import com.roadmap.backend.review.dto.ReviewDeleteResponse;
import com.roadmap.backend.review.dto.ReviewRegisterRequest;
import com.roadmap.backend.review.dto.ReviewRegisterResponse;
import com.roadmap.backend.review.exception.ReviewException;
import com.roadmap.backend.review.service.ReviewService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user/reviews")
@Tag(name = "Review", description = "후기 관련 API")
@RequiredArgsConstructor
public class ReviewController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ReviewService reviewService;

    @PostMapping
    @Operation(
            summary = "후기 등록",
            description = """
                    후기 등록을 진행합니다. 우측 상단 Authorize에서 인증 토큰을 등록해주세요.
                    
                    **인증**
                    - Authorization 헤더에 Bearer 토큰 필수 (휴대폰 인증 후 발급받은 verificationToken)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReviewRegisterResponse> registerReview(
            @Valid @RequestBody ReviewRegisterRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);
        ReviewRegisterResponse response = reviewService.registerReview(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(
            summary = "후기 삭제",
            description = """
                    후기 삭제를 진행합니다. 우측 상단 Authorize에서 인증 토큰을 등록해주세요.
                    
                    **인증**
                    - Authorization 헤더에 Bearer 토큰 필수 (휴대폰 인증 후 발급받은 verificationToken)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReviewDeleteResponse> deleteReview(
            @Parameter(name = "reviewId", in = ParameterIn.PATH, required = true, description = "후기 ID",
                    schema = @Schema(type = "long", example = "1"))
            @PathVariable Long reviewId,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        ReviewDeleteResponse response = reviewService.deleteReview(reviewId, token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/mine")
    @Operation(
            summary = "내 후기 목록 조회",
            description = """
                    내 후기 목록을 조회합니다. 우측 상단 Authorize에서 인증 토큰을 등록해주세요.
                    
                    **인증**
                    - Authorization 헤더에 Bearer 토큰 필수 (휴대폰 인증 후 발급받은 verificationToken)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MyReviewsResponse> getMyReviews(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        String token = extractToken(authHeader);
        MyReviewsResponse response = reviewService.getMyReviews(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new ReviewException("Authorization 헤더에 Bearer 토큰이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
