package com.roadmap.backend.waitlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "학생 대기 등록 응답")
public class WaitlistRegisterResponse {

    @Schema(description = "성공 여부")
    private Boolean success;

    @Schema(description = "메시지")
    private String message;

    @Schema(description = "대기열 ID")
    private Long waitlistId;

    @Schema(description = "등록 일시")
    private LocalDateTime registeredAt;
}
