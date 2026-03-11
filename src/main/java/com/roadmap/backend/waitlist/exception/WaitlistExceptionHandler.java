package com.roadmap.backend.waitlist.exception;

import com.roadmap.backend.waitlist.dto.WaitlistRegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WaitlistExceptionHandler {

    @ExceptionHandler(WaitlistException.class)
    public ResponseEntity<WaitlistRegisterResponse> handleWaitlistException(WaitlistException ex) {
        WaitlistRegisterResponse response = WaitlistRegisterResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .waitlistId(null)
                .registeredAt(null)
                .build();
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}
