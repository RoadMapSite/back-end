package com.roadmap.backend.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "내 후기 정보")
public class MyReviewItem {

    @Schema(description = "후기 ID", example = "105")
    private Long reviewId;

    @Schema(description = "지점", example = "N")
    private String branch;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "작성자명")
    private String authorName;

    @Schema(description = "후기 상태 (PENDING / APPROVED)")
    private String status;

    @Schema(description = "작성일")
    private LocalDateTime createdAt;
}
