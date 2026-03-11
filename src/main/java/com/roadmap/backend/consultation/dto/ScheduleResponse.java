package com.roadmap.backend.consultation.dto;

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
@Schema(description = "월별 상담 스케줄 조회 응답 (예약 불가능한 시간 반환 방식)")
public class ScheduleResponse {

    @Schema(description = "지점 구분 (N 또는 HI_END)", example = "N")
    private String branch;

    @Schema(description = "조회한 연월 (yyyy-MM)", example = "2026-03")
    private String yearMonth;

    @Schema(description = "영업시간 정보 (프론트엔드에서 전체 슬롯 생성 시 참고)")
    private OperatingHours operatingHours;

    @Schema(description = "예약 불가능한 날짜·시간 목록 (이미 예약된 시간만 포함, 비어있는 날짜는 제외)")
    private List<UnavailableScheduleItem> unavailableSchedules;
}
