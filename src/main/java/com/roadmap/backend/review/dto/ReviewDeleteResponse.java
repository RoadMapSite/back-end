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
@Schema(description = "후기 삭제 응답")
public class ReviewDeleteResponse {

    @Schema(description = "성공 여부")
    private Boolean success;

    @Schema(description = "메시지")
    private String message;
}
