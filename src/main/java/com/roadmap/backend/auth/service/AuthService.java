package com.roadmap.backend.auth.service;

import com.roadmap.backend.auth.dto.VerificationRequest;
import com.roadmap.backend.auth.dto.VerificationResponse;
import com.roadmap.backend.auth.dto.VerificationVerifyRequest;
import com.roadmap.backend.auth.dto.VerificationVerifyResponse;
import com.roadmap.backend.auth.entity.PhoneVerification;
import com.roadmap.backend.auth.repository.PhoneVerificationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final int AUTH_CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 60;

    private final PhoneVerificationRepository phoneVerificationRepository;

    @Transactional
    public VerificationResponse sendVerificationCode(VerificationRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String authCode = generateAuthCode();

        PhoneVerification phoneVerification = PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .authCode(authCode)
                .isVerified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .createdAt(LocalDateTime.now())
                .build();

        phoneVerificationRepository.save(phoneVerification);

        log.info("인증번호 [{}] 발송 완료", authCode);

        return VerificationResponse.builder()
                .success(true)
                .message("인증번호가 발송되었습니다.")
                .build();
    }

    @Transactional
    public VerificationVerifyResponse verifyCode(VerificationVerifyRequest request) {
        Optional<PhoneVerification> verificationOpt = phoneVerificationRepository
                .findTopByPhoneNumberOrderByCreatedAtDesc(request.getPhoneNumber());

        if (verificationOpt.isEmpty()) {
            return VerificationVerifyResponse.builder()
                    .success(false)
                    .message("인증 요청을 찾을 수 없습니다. 먼저 인증번호 발송을 요청해주세요.")
                    .verificationToken(null)
                    .build();
        }

        PhoneVerification verification = verificationOpt.get();

        if (verification.getIsVerified()) {
            return VerificationVerifyResponse.builder()
                    .success(false)
                    .message("이미 인증이 완료된 요청입니다.")
                    .verificationToken(null)
                    .build();
        }

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            return VerificationVerifyResponse.builder()
                    .success(false)
                    .message("인증번호가 만료되었습니다. 다시 발송 요청을 해주세요.")
                    .verificationToken(null)
                    .build();
        }

        if (!verification.getAuthCode().equals(request.getAuthCode())) {
            return VerificationVerifyResponse.builder()
                    .success(false)
                    .message("인증번호가 일치하지 않습니다.")
                    .verificationToken(null)
                    .build();
        }

        String token = UUID.randomUUID().toString();
        verification.completeVerification(token);
        phoneVerificationRepository.save(verification);

        return VerificationVerifyResponse.builder()
                .success(true)
                .message("인증이 완료되었습니다.")
                .verificationToken(token)
                .build();
    }

    private String generateAuthCode() {
        int min = (int) Math.pow(10, AUTH_CODE_LENGTH - 1);
        int max = (int) Math.pow(10, AUTH_CODE_LENGTH) - 1;
        int code = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.valueOf(code);
    }
}
