package com.roadmap.backend.admin.exception;

import com.roadmap.backend.admin.dto.AdminLoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdminAuthExceptionHandler {

    @ExceptionHandler(AdminAuthException.class)
    public ResponseEntity<AdminLoginResponse> handleAdminAuthException(AdminAuthException ex) {
        AdminLoginResponse response = AdminLoginResponse.builder()
                .success(false)
                .accessToken(null)
                .build();
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }
}
