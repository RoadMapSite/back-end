package com.roadmap.backend.admin.dto;

import com.roadmap.backend.domain.Grade;
import com.roadmap.backend.waitlist.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "관리자 대기열 직접 등록 요청")
public class AdminWaitlistCreateRequest {

    @NotBlank(message = "시즌은 필수입니다.")
    @Schema(description = "시즌 (SUMMER, WINTER, SEMESTER_1, SEMESTER_2)", required = true, example = "SEMESTER_1")
    private String season;

    @Schema(description = "지점 (SEMESTER 시즌일 때 필수: N 또는 Hi-end, 캠프 시즌은 생략)", example = "N")
    private String branch;

    @NotBlank(message = "학생 이름은 필수입니다.")
    @Schema(description = "학생 이름", required = true, example = "홍길동")
    private String name;

    @NotNull(message = "성별은 필수입니다.")
    @Schema(description = "성별", required = true, example = "MALE")
    private Gender gender;

    @NotNull(message = "기존 재원생 여부는 필수입니다.")
    @Schema(description = "기존 재원생 여부", required = true, example = "false")
    private Boolean isExisting;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Schema(description = "휴대폰 번호", required = true, example = "01012345678")
    private String phoneNumber;

    @Schema(description = "학생 나이 (N수관 또는 캠프 시 필수 조건)", example = "17")
    private Integer age;

    @Schema(description = "학교명 (Hi-end 또는 캠프 시 필수 조건)", example = "OO고등학교")
    private String school;

    @Schema(description = "학년 (Hi-end 또는 캠프 시 필수 조건)", example = "2학년")
    private Grade grade;
}
