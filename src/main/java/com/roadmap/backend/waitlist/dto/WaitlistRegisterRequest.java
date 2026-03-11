package com.roadmap.backend.waitlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학생 대기 등록 요청")
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

    @NotNull(message = "학생 나이는 필수입니다.")
    @Schema(description = "학생 나이", required = true, example = "15")
    private Integer age;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Schema(description = "휴대폰 번호", required = true, example = "01012345678")
    private String phoneNumber;
}
