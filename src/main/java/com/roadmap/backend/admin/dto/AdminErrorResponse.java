package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "관리자 API 에러 응답")
public class AdminErrorResponse {

    @Schema(description = "성공 여부")
    private Boolean success;

    @Schema(description = "에러 메시지")
    private String message;
}
