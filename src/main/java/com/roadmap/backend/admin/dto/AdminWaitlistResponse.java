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
@Schema(description = "관리자 대기열 리스트 조회 응답")
public class AdminWaitlistResponse {

    @Schema(description = "대기열 목록")
    private List<WaitlistDetail> waitlists;
}
