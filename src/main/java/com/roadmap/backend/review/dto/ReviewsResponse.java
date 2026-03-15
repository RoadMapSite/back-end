package com.roadmap.backend.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "전체 후기 목록 조회 응답")
public class ReviewsResponse {
    @Schema(description = "현재 페이지", example = "1")
    private Integer currentPage;

    @Schema(description = "전체 페이지 수", example = "5")
    private Integer totalPages;

    @Schema(description = "후기 목록")
    private List<ReviewSummary> reviews;
}
