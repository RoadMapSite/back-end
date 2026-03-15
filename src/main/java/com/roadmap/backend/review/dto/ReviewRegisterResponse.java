package com.roadmap.backend.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "후기 등록 응답")
public class ReviewRegisterResponse {

    @Schema(description = "성공 여부")
    private Boolean success;

    @Schema(description = "메시지")
    private String message;

    @Schema(description = "후기 ID")
    private Long reviewId;
}
