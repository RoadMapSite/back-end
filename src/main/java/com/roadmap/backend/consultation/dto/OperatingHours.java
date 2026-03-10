package com.roadmap.backend.consultation.dto;

import com.roadmap.backend.consultation.config.ConsultationConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상담 영업시간 정보")
public class OperatingHours {

    @Schema(description = "영업 시작 시간", example = "10:00")
    private String start;

    @Schema(description = "영업 종료 시간", example = "18:00")
    private String end;

    @Schema(description = "예약 단위(분)", example = "30")
    private Integer intervalMinutes;

    public static OperatingHours createDefault() {
        return OperatingHours.builder()
                .start(ConsultationConfig.OPERATING_START)
                .end(ConsultationConfig.OPERATING_END)
                .intervalMinutes(ConsultationConfig.INTERVAL_MINUTES)
                .build();
    }
}
