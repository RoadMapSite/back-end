package com.roadmap.backend.admin.dto;

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
@Schema(description = "대기열 상세 정보")
public class WaitlistDetail {

    @Schema(description = "대기열 ID")
    private Long waitlistId;

    @Schema(description = "대기 순번")
    private Integer waitingNumber;

    @Schema(description = "지점 (N 또는 Hi-end, SUMMER/WINTER 시 null)")
    private String branch;

    @Schema(description = "시즌")
    private String season;

    @Schema(description = "학생 이름")
    private String name;

    @Schema(description = "학생 나이")
    private Integer age;

    @Schema(description = "휴대폰 번호")
    private String phoneNumber;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "등록 일시")
    private LocalDateTime registeredAt;
}
