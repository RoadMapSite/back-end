package com.roadmap.backend.consultation.dto;

import com.roadmap.backend.consultation.entity.Branch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {

    @NotNull(message = "지점은 필수이며, N 또는 HI_END만 입력 가능합니다.")
    @Schema(description = "지점 구분. N(일반) 또는 HI_END(Hi-end) 중 하나만 입력 가능", example = "N", allowableValues = {"N", "HI_END"})
    private Branch branch;

    @NotNull(message = "상담 날짜는 필수입니다.")
    private LocalDate date;

    @NotBlank(message = "상담 시간은 필수이며, 30분 단위(예: 18:00, 18:30)만 입력 가능합니다.")
    @Schema(description = "상담 시간. 30분 단위만 가능 (예: 09:00, 09:30, 18:00)", example = "18:00")
    private String time;

    @NotBlank(message = "학생 이름은 필수입니다.")
    private String name;

    @NotNull(message = "학생 나이는 필수입니다.")
    private Integer age;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    private String phoneNumber;
}
