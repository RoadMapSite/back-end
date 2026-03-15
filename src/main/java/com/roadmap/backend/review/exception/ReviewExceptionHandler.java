package com.roadmap.backend.review.exception;

import com.roadmap.backend.review.dto.ReviewRegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReviewExceptionHandler {

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ReviewRegisterResponse> handleReviewException(ReviewException ex) {
        ReviewRegisterResponse response = ReviewRegisterResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .reviewId(null)
                .build();
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}
