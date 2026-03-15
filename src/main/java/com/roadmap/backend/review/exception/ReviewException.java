package com.roadmap.backend.review.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ReviewException extends RuntimeException{

    private final HttpStatus httpStatus;

    public ReviewException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public ReviewException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
