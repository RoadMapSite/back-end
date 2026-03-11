package com.roadmap.backend.admin.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class AdminAuthException extends RuntimeException {

    private final HttpStatus httpStatus;

    public AdminAuthException(String message) {
        super(message);
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }

    public AdminAuthException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
