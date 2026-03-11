package com.roadmap.backend.waitlist.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class WaitlistException extends RuntimeException {

    private final HttpStatus httpStatus;

    public WaitlistException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public WaitlistException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
