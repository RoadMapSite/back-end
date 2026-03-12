package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "후기 승인 상태 변경 요청")
public class ReviewStatusUpdateRequest {

    @NotBlank(message = "승인 상태는 필수입니다.")
    @Pattern(regexp = "^(PENDING|APPROVED)$",
            message = "승인 상태는 PENDING 또는 APPROVED 중 하나만 입력 가능합니다.")
    @Schema(description = "변경할 승인 상태 (PENDING: 대기, APPROVED: 승인)", required = true,
            example = "APPROVED", allowableValues = {"PENDING", "APPROVED"})
    private String status;
}
