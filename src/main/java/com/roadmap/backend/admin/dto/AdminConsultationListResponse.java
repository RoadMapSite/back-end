package com.roadmap.backend.admin.dto;

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
@Schema(description = "관리자 상담 신청 리스트 조회 응답")
public class AdminConsultationListResponse {

    @Schema(description = "상담 신청 목록")
    private List<ConsultationDetail> consultations;
}
