package com.roadmap.backend.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "이미지 업로드 응답")
public class ImageUploadResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "S3 퍼블릭 URL", example = "https://s3.ap-northeast-2.amazonaws.com/bucket/reviews/uuid.jpg")
    private String imageUrl;
}
