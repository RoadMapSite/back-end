package com.roadmap.backend.waitlist.dto;

import com.roadmap.backend.domain.Grade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
        학생 대기 등록 요청.
        - SUMMER, WINTER: 나이(age) 또는 학교+학년(school, grade) 중 하나만 입력
        - SEMESTER_1, SEMESTER_2 + N: 나이(age) 필수
        - SEMESTER_1, SEMESTER_2 + Hi-end: 학교(school), 학년(grade) 필수 (학년: 2학년, 3학년만)
        """)
public class WaitlistRegisterRequest {

    @Schema(description = "지점 구분 (SEMESTER 시즌일 때만 필수: N 또는 Hi-end)", example = "N")
    private String branch;

    @NotBlank(message = "시즌은 필수입니다. (SUMMER, WINTER, SEMESTER_1, SEMESTER_2)")
    @Pattern(regexp = "^(SUMMER|WINTER|SEMESTER_1|SEMESTER_2)$",
            message = "시즌은 SUMMER, WINTER, SEMESTER_1, SEMESTER_2 중 하나만 입력 가능합니다.")
    @Schema(description = "시즌", required = true, example = "SUMMER", allowableValues = {"SUMMER", "WINTER", "SEMESTER_1", "SEMESTER_2"})
    private String season;

    @NotBlank(message = "학생 이름은 필수입니다.")
    @Schema(description = "학생 이름", required = true, example = "홍길동")
    private String name;

    @Schema(description = "학생 나이 (N수관 또는 SUMMER/WINTER에서 나이 선택 시 필수)", example = "15")
    private Integer age;

    @Schema(description = "학교명 (Hi-end 또는 SUMMER/WINTER에서 학교+학년 선택 시 필수, 자유 입력)", example = "OO고등학교")
    private String school;

    @Schema(description = "학년 (Hi-end 또는 SUMMER/WINTER에서 학교+학년 선택 시 필수, 2학년 또는 3학년만)", example = "2학년", allowableValues = {"2학년", "3학년"})
    private Grade grade;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Schema(description = "휴대폰 번호", required = true, example = "01012345678")
    private String phoneNumber;
}
