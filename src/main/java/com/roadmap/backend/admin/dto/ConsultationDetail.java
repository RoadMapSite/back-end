package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상담 신청 상세 정보")
public class ConsultationDetail {

    @Schema(description = "상담 ID")
    private Long consultationId;

    @Schema(description = "지점 (N 또는 Hi-end)")
    private String branch;

    @Schema(description = "상담 날짜")
    private LocalDate date;

    @Schema(description = "상담 시간")
    private String time;

    @Schema(description = "학생 이름")
    private String name;

    @Schema(description = "학생 나이")
    private Integer age;

    @Schema(description = "휴대폰 번호")
    private String phoneNumber;

    @Schema(description = "등록 일시")
    private LocalDateTime registeredAt;
}
