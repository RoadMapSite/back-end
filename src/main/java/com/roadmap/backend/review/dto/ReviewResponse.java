package com.roadmap.backend.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "개별 후기 상세 조회 응답")
public class ReviewResponse {

    @Schema(description = "후기 ID")
    private Long reviewId;

    @Schema(description = "지점")
    private String branch;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문 내용")
    private String content;

    @Schema(description = "작성자명")
    private String authorName;

    @Schema(description = "이미지 url")
    private List<String> imageUrls;

    @Schema(description = "조회수")
    private Integer viewCount;

    @Schema(description = "created_at")
    private LocalDateTime createdAt;
}
