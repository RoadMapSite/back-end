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
@Schema(description = "대기열 상태 변경 요청")
public class WaitlistStatusUpdateRequest {

    @NotBlank(message = "상태는 필수입니다.")
    @Pattern(regexp = "^(WAITING|CONTACTED|REGISTERED|CANCELED)$",
            message = "상태는 WAITING, CONTACTED, REGISTERED, CANCELED 중 하나만 입력 가능합니다.")
    @Schema(description = "변경할 상태", required = true, example = "CONTACTED",
            allowableValues = {"WAITING", "CONTACTED", "REGISTERED", "CANCELED"})
    private String status;
}
