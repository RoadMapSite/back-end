package com.roadmap.backend.image.controller;

import com.roadmap.backend.image.dto.ImageUploadResponse;
import com.roadmap.backend.image.service.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/images")
@Tag(name = "Image", description = "이미지 업로드 API")
@RequiredArgsConstructor
public class ImageController {

    private final S3Uploader s3Uploader;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "이미지 업로드 (S3)",
            description = "multipart/form-data 파트 `image`에 바이너리 파일을 넣어 전송하면 S3에 저장 후 퍼블릭 URL을 반환합니다.")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @Parameter(
                    name = "image",
                    description = "업로드할 이미지 파일 (jpg, jpeg, png, gif, webp)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("image") MultipartFile image) {

        String url = s3Uploader.upload(image);
        return ResponseEntity.ok(ImageUploadResponse.builder()
                .success(true)
                .imageUrl(url)
                .build());
    }
}
