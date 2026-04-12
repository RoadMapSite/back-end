package com.roadmap.backend.admin.exception;

import com.roadmap.backend.admin.controller.AdminAuthController;
import com.roadmap.backend.admin.dto.AdminErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AdminAuthController.class)
public class AdminAuthExceptionHandler {

    @ExceptionHandler(AdminAuthException.class)
    public ResponseEntity<?> handleAdminAuthException(AdminAuthException ex) {
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.UNAUTHORIZED;

        AdminErrorResponse response = AdminErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<AdminErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        AdminErrorResponse response = AdminErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AdminErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        AdminErrorResponse response = AdminErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
