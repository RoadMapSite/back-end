package com.roadmap.backend.image.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ImageUploadException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ImageUploadException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public ImageUploadException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
