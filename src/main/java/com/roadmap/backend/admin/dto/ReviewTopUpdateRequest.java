package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "우수 후기 지정/해제 요청")
public class ReviewTopUpdateRequest {

    @NotNull(message = "우수 후기 지정 여부는 필수입니다.")
    @Schema(description = "우수 후기 지정 여부", required = true, example = "true")
    private Boolean isTop;
}
