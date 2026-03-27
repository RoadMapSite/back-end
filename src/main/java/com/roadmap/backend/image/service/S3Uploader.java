package com.roadmap.backend.image.service;

import com.roadmap.backend.global.config.CloudAwsProperties;
import com.roadmap.backend.image.exception.ImageUploadException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Uploader {

    private static final String KEY_PREFIX = "reviews/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final S3Client s3Client;
    private final CloudAwsProperties cloudAwsProperties;

    public S3Uploader(ObjectProvider<S3Client> s3ClientProvider, CloudAwsProperties cloudAwsProperties) {
        this.s3Client = s3ClientProvider.getIfAvailable();
        this.cloudAwsProperties = cloudAwsProperties;
    }

    public String upload(MultipartFile file) {
        if (s3Client == null) {
            throw new ImageUploadException(
                    "S3가 구성되지 않았습니다. cloud.aws.s3.bucket과 AWS 자격 증명을 설정하세요.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        validateMultipartFile(file);

        String ext = extractExtension(file.getOriginalFilename());
        String contentType = resolveContentType(file, ext);
        String key = KEY_PREFIX + UUID.randomUUID() + "." + ext;

        String bucket = cloudAwsProperties.getS3().getBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new ImageUploadException("S3 버킷이 설정되지 않았습니다. cloud.aws.s3.bucket (또는 AWS_S3_BUCKET)을 확인하세요.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucket.trim())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(put, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException e) {
            throw new ImageUploadException("파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }

        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(bucket.trim())
                        .key(key)
                        .build())
                .toExternalForm();
    }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null) {
            throw new ImageUploadException("업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if (file.isEmpty()) {
            throw new ImageUploadException("빈 파일은 업로드할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            throw new ImageUploadException("파일명이 올바르지 않습니다.");
        }
        String ext = extractExtension(original);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ImageUploadException("허용되지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 가능합니다.)");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ImageUploadException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT).trim();
    }

    private String resolveContentType(MultipartFile file, String ext) {
        String ct = file.getContentType();
        if (StringUtils.hasText(ct)) {
            return ct;
        }
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
