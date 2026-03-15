package com.roadmap.backend.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "후기 등록 요청")
public class ReviewRegisterRequest {

    @Schema(description = "지점 구분 (N 또는 Hi-end)", example = "N")
    private String branch;

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", required = true, example = "강추합니다.")
    private String title;

    @NotBlank(message = "본문 내용은 필수입니다.")
    @Schema(description = "내용", required = true, example = "좋아요.")
    private String content;

        @Schema(description = "S3에 먼저 업로드된 이미지 URL 배열", example = "[\"https://s3.ap-northeast-2.amazonaws.com/your-bucket/reviews/img1.jpg\"]")
    private List<String> imageUrls;

    @NotBlank(message = "학생 이름은 필수입니다.")
    @Schema(description = "학생 이름", required = true, example = "홍길동")
    private String name;
}
