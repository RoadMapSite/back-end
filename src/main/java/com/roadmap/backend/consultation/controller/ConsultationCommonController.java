package com.roadmap.backend.consultation.controller;

import com.roadmap.backend.consultation.dto.ScheduleResponse;
import com.roadmap.backend.consultation.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/common/consultations")
@Tag(name = "Consultation", description = "상담 관리 API")
@RequiredArgsConstructor
public class ConsultationCommonController {

    private final ConsultationService consultationService;

    @GetMapping
    @Operation(
            summary = "월별 상담 불가능 스케줄 조회",
            description = """
                    비회원도 조회 가능한 공통 API입니다. **예약 불가능한 시간(Booked Times)** 방식으로 응답합니다.
                    
                    ## 프론트엔드 연동 가이드
                    1. **operatingHours**를 참고하여 10:00~18:00 구간의 전체 타임 슬롯(30분 단위: 10:00, 10:30, ... 17:30)을 그립니다.
                    2. **unavailableSchedules**에 포함된 날짜·시간은 이미 예약되어 있으므로 **Disabled 처리**합니다.
                    3. unavailableSchedules에 없는 날짜·시간은 모두 예약 가능합니다.
                    4. 예약이 없는 날짜는 unavailableSchedules 배열에 아예 포함되지 않습니다.
                    
                    **파라미터**
                    - **branch**: 지점 구분 (N 또는 Hi-end)
                    - **yearMonth**: 조회 연월 (예: 2026-03)
                    """
    )
    public ResponseEntity<ScheduleResponse> getUnavailableSchedules(
            @Parameter(description = "지점 구분 (N 또는 Hi-end)", required = true)
            @RequestParam(name = "branch") String branch,
            @Parameter(description = "조회할 연월 (yyyy-MM 형식)", required = true, example = "2026-03")
            @RequestParam(name = "yearMonth") String yearMonth) {

        ScheduleResponse response = consultationService.getUnavailableSchedules(branch, yearMonth);
        return ResponseEntity.ok(response);
    }
}
