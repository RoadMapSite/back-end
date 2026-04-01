package com.roadmap.backend.admin.dto;

import com.roadmap.backend.domain.Grade;
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

    @Schema(description = "학생 나이 (N수관 또는 나이 선택 시)")
    private Integer age;

    @Schema(description = "학교명 (Hi-end 또는 학교·학년 선택 시)")
    private String school;

    @Schema(description = "학년 (Hi-end 또는 학교·학년 선택 시, 2학년 또는 3학년)")
    private Grade grade;

    @Schema(description = "휴대폰 번호")
    private String phoneNumber;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "등록 일시")
    private LocalDateTime registeredAt;

    @Schema(description = "기존 재원생 여부")
    private Boolean isExisting;
}
