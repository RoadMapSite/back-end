package com.roadmap.backend.waitlist.controller;

import com.roadmap.backend.waitlist.dto.WaitlistRegisterRequest;
import com.roadmap.backend.waitlist.dto.WaitlistRegisterResponse;
import com.roadmap.backend.waitlist.exception.WaitlistException;
import com.roadmap.backend.waitlist.service.WaitlistService;
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
@RequestMapping("/v1/user/waitlists")
@Tag(name = "Waitlist", description = "대기열 관리 API")
@RequiredArgsConstructor
public class UserWaitlistController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final WaitlistService waitlistService;

    @PostMapping
    @Operation(
            summary = "시즌별 대기 등록",
            description = """
                    시즌별 대기열 등록을 진행합니다. 우측 상단 Authorize에서 인증 토큰을 등록해주세요.
                    
                    **인증**
                    - Authorization 헤더에 Bearer 토큰 필수 (휴대폰 인증 후 발급받은 verificationToken)
                    - 토큰 내 휴대폰 번호와 Request Body의 phoneNumber 일치 검증
                    
                    **시즌별 branch 규칙**
                    - SUMMER, WINTER: branch 무시 (null)
                    - SEMESTER_1, SEMESTER_2: branch 필수 (N 또는 Hi-end)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<WaitlistRegisterResponse> registerWaitlist(
            @Valid @RequestBody WaitlistRegisterRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);
        WaitlistRegisterResponse response = waitlistService.registerWaitlist(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new WaitlistException("Authorization 헤더에 Bearer 토큰이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
