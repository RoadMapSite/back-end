package com.roadmap.backend.consultation.controller;

import com.roadmap.backend.consultation.dto.ConsultationRequest;
import com.roadmap.backend.consultation.dto.ConsultationResponse;
import com.roadmap.backend.consultation.exception.ConsultationException;
import com.roadmap.backend.consultation.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user/consultations")
@Tag(name = "Consultation", description = "상담 관리 API")
@RequiredArgsConstructor
public class ConsultationController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ConsultationService consultationService;

    @PostMapping
    @Operation(
            summary = "상담 신청서 등록",
            description = """
                    상담 예약을 확정합니다. 우측 상단 Authorize에서 인증 토큰을 등록해주세요.
                    
                    **입력 규칙**
                    - **branch**: N 또는 Hi-end. N → 나이(age) 필수, Hi-end → 학교(school)·학년(grade) 필수 (학년: 2학년, 3학년만)
                    - **date**: 오늘 기준 **이틀 뒤 날짜부터** 예약 가능. 일요일 불가.
                    - **time**: 지점·요일별 고정 슬롯만 허용 — N: 평일·토 10:00, 17:00 / Hi-end: 평일 17:00, 20:00 / 토요일 10:00, 16:00
                    
                    **유효성 검사**
                    - 위 날짜·시간·일요일·최소 이틀 뒤 규칙, 중복 예약 차단
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ConsultationResponse> registerConsultation(
            @Valid @RequestBody ConsultationRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);
        ConsultationResponse response = consultationService.registerConsultation(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new ConsultationException("Authorization 헤더에 Bearer 토큰이 필요합니다.");
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
