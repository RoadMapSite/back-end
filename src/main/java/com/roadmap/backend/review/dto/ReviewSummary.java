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
@Schema(description = "후기 요약 정보")
public class ReviewSummary {

    @Schema(description = "후기 ID", example = "102")
    private Long reviewId;

    @Schema(description = "후기 제목", example = "시설이 정말 쾌적하고 집중이 잘 됩니다!")
    private String title;

    @Schema(description = "작성자 이름", example = "이*훈")
    private String authorName;

    @Schema(description = "조회수", example = "152")
    private Integer viewCount;

    @Schema(description = "작성일", example = "2026-02-21T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "isTop", example = "false")
    private Boolean isTop;
}
