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
@Schema(description = "내 후기 목록 조회 응답")
public class MyReviewsResponse {

    @Schema(description = "내 후기 목록")
    private List<MyReviewItem> myReviews;
}
