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
@Schema(description = "특정 날짜의 예약 불가능한(이미 차있는) 시간 목록")
public class UnavailableScheduleItem {

    @Schema(description = "날짜 (yyyy-MM-dd)", example = "2026-03-12")
    private String date;

    @Schema(description = "해당 날짜에 이미 예약되어 선택 불가한 시간 목록 (30분 단위)", example = "[\"11:00\", \"14:30\"]")
    private List<String> bookedTimes;
}
