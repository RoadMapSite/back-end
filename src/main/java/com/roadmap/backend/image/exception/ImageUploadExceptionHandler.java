package com.roadmap.backend.image.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ImageUploadExceptionHandler {

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<Map<String, Object>> handle(ImageUploadException ex) {
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage()
                ));
    }
}
