package com.roadmap.backend.consultation.dto;

import com.roadmap.backend.consultation.entity.Branch;
import com.roadmap.backend.domain.Grade;
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
@Schema(description = """
        상담 신청 요청.
        - N(N수관): 나이(age) 필수
        - Hi-end: 학교(school), 학년(grade) 필수 (학년: 2학년, 3학년만)
        """)
public class ConsultationRequest {

    @NotNull(message = "지점은 필수이며, N 또는 Hi-end만 입력 가능합니다.")
    @Schema(description = "지점 구분. N(일반) 또는 Hi-end 중 하나만 입력 가능", example = "N", allowableValues = {"N", "Hi-end"})
    private Branch branch;

    @NotNull(message = "상담 날짜는 필수입니다.")
    private LocalDate date;

    @NotBlank(message = "상담 시간은 필수이며, 영업시간 10:00~17:30 내 30분 단위만 입력 가능합니다.")
    @Schema(description = "상담 시간. 영업시간 10:00~17:30 내 30분 단위 (예: 10:00, 10:30, 17:30)", example = "10:00")
    private String time;

    @NotBlank(message = "학생 이름은 필수입니다.")
    private String name;

    @Schema(description = "학생 나이 (N수관일 때 필수)", example = "15")
    private Integer age;

    @Schema(description = "학교명 (Hi-end일 때 필수, 자유 입력)", example = "OO고등학교")
    private String school;

    @Schema(description = "학년 (Hi-end일 때 필수, 2학년 또는 3학년만)", example = "2학년", allowableValues = {"2학년", "3학년"})
    private Grade grade;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    private String phoneNumber;
}
