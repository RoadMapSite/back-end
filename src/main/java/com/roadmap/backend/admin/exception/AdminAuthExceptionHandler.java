package com.roadmap.backend.admin.exception;

import com.roadmap.backend.admin.dto.AdminErrorResponse;
import com.roadmap.backend.admin.dto.AdminLoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdminAuthExceptionHandler {

    @ExceptionHandler(AdminAuthException.class)
    public ResponseEntity<?> handleAdminAuthException(AdminAuthException ex) {
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.UNAUTHORIZED;

        if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
            AdminLoginResponse response = AdminLoginResponse.builder()
                    .success(false)
                    .accessToken(null)
                    .build();
            return ResponseEntity.status(status).body(response);
        }

        AdminErrorResponse response = AdminErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
