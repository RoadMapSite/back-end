package com.roadmap.backend.consultation.exception;

import com.roadmap.backend.consultation.dto.ConsultationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConsultationExceptionHandler {

    @ExceptionHandler(ConsultationException.class)
    public ResponseEntity<ConsultationResponse> handleConsultationException(ConsultationException ex) {
        ConsultationResponse response = ConsultationResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .consultationId(null)
                .registeredAt(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
