package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "관리자용 후기 상세 조회 응답 (조회수 미증가, 실명)")
public class AdminReviewDetailResponse {

    @Schema(description = "후기 ID", example = "14")
    private Long reviewId;

    @Schema(description = "제목", example = "로드맵 후기")
    private String title;

    @Schema(description = "본문", example = "명실상부 동백 최고의 관리형 독서실 로드맵 !! ...")
    private String content;

    @Schema(description = "작성자 실명 (마스킹 없음)", example = "김도현")
    private String authorName;

    @Schema(description = "이미지 URL 목록")
    private List<String> imageUrls;

    @Schema(description = "조회수", example = "153")
    private Integer viewCount;

    @Schema(description = "승인 여부", example = "true")
    private Boolean isApproved;

    @Schema(description = "우수 후기 여부", example = "false")
    private Boolean isBest;

    @Schema(description = "작성일시 (UTC, ISO-8601)", example = "2026-03-18T05:30:00Z")
    private String createdAt;
}
