package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 후기 목록 API용 DTO.
 * <p>
 * 두 타입을 한 파일에 두면 Eclipse 등에서 출력 폴더(bin) 동기화 시 일부 클래스만 누락되는 문제를 줄일 수 있다.
 */
public final class AdminReviewModels {

    private AdminReviewModels() {
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "관리자용 후기 목록 항목 (실명, 전체 상태)")
    public static class ReviewItem {

        @Schema(description = "후기 ID", example = "14")
        private Long reviewId;

        @Schema(description = "제목", example = "로드맵 후기")
        private String title;

        @Schema(description = "작성자 실명 (마스킹 없음)", example = "김도현")
        private String authorName;

        @Schema(description = "작성일 (날짜만)", example = "2026-03-18")
        private String createdAt;

        @Schema(description = "승인 여부 (status가 APPROVED인 경우 true)", example = "true")
        private Boolean isApproved;

        @Schema(description = "우수 후기 여부", example = "false")
        private Boolean isBest;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "관리자 전체 후기 페이징 응답")
    public static class PageResponse {

        @Schema(description = "현재 페이지 (1부터)", example = "1")
        private Integer currentPage;

        @Schema(description = "전체 페이지 수", example = "5")
        private Integer totalPages;

        @Schema(description = "전체 요소 수", example = "48")
        private Long totalElements;

        @Schema(description = "후기 목록")
        private List<ReviewItem> reviews;
    }
}
