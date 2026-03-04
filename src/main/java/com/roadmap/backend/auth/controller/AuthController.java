package com.roadmap.backend.auth.controller;

import com.roadmap.backend.auth.dto.VerificationRequest;
import com.roadmap.backend.auth.dto.VerificationResponse;
import com.roadmap.backend.auth.dto.VerificationVerifyRequest;
import com.roadmap.backend.auth.dto.VerificationVerifyResponse;
import com.roadmap.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/common/auth")
@Tag(name = "Auth", description = "휴대폰 인증 관리 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send")
    @Operation(summary = "휴대폰 인증번호 발송", description = "POST /v1/common/auth/send - 6자리 인증번호를 생성하여 발송합니다. (현재 콘솔 로그로 대체)")
    public ResponseEntity<VerificationResponse> sendVerificationCode(
            @Valid @RequestBody VerificationRequest request) {
        VerificationResponse response = authService.sendVerificationCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(
            summary = "휴대폰 인증번호 검증 및 토큰 발급",
            description = "POST /v1/common/auth/verify - 사용자가 입력한 인증번호를 확인하고, 일치할 경우 상담 신청 시 필요한 verificationToken을 발급합니다."
    )
    public ResponseEntity<VerificationVerifyResponse> verifyCode(
            @Valid @RequestBody VerificationVerifyRequest request) {
        VerificationVerifyResponse response = authService.verifyCode(request);
        return ResponseEntity.ok(response);
    }
}
