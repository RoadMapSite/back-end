package com.roadmap.backend.consultation.exception;

import com.roadmap.backend.consultation.controller.ConsultationController;
import com.roadmap.backend.consultation.dto.ConsultationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = ConsultationController.class)
public class ConsultationExceptionHandler {

    @ExceptionHandler(ConsultationException.class)
    public ResponseEntity<ConsultationResponse> handleConsultationException(ConsultationException ex) {
        log.warn("상담 비즈니스 예외: {}", ex.getMessage());
        ConsultationResponse response = ConsultationResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .consultationId(null)
                .registeredAt(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * JSON 본문 파싱/역직렬화 실패 시 (컨트롤러·서비스 진입 전). Hi-end 등 enum 매핑 문제도 여기서 잡힌다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ConsultationResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        log.warn(
                "상담 API 요청 본문 역직렬화 실패 (컨트롤러 미진입): message={}, cause={}",
                ex.getMessage(),
                cause != null ? cause.getMessage() : "null",
                ex);
        String detail = cause != null ? cause.getMessage() : ex.getMessage();
        ConsultationResponse response = ConsultationResponse.builder()
                .success(false)
                .message("요청 본문 형식이 올바르지 않습니다. branch는 N 또는 Hi-end, 학년은 2학년/3학년, 날짜는 yyyy-MM-dd 형식인지 확인해 주세요."
                        + (detail != null ? " [" + detail + "]" : ""))
                .consultationId(null)
                .registeredAt(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ConsultationResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("상담 API @Valid 검증 실패: {}", ex.getMessage());
        String first = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("입력값을 확인해 주세요.");
        ConsultationResponse response = ConsultationResponse.builder()
                .success(false)
                .message(first)
                .consultationId(null)
                .registeredAt(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
