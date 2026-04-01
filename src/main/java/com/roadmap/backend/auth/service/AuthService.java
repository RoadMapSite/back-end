package com.roadmap.backend.auth.service;

import com.roadmap.backend.auth.dto.VerificationRequest;
import com.roadmap.backend.auth.dto.VerificationResponse;
import com.roadmap.backend.auth.dto.VerificationVerifyRequest;
import com.roadmap.backend.auth.dto.VerificationVerifyResponse;
import com.roadmap.backend.auth.entity.PhoneVerification;
import com.roadmap.backend.auth.repository.PhoneVerificationRepository;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final int AUTH_CODE_LENGTH = 6;
    private static final int REDIS_TTL_SECONDS = 180;
    private static final String REDIS_KEY_PREFIX = "auth:sms:";
    private static final int EXPIRATION_MINUTES = 60;

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    /** 휴대폰 인증 문자 발신번호 — N수관 번호(solapi.sender.number.n)와 동일 */
    @Value("${solapi.sender.number.n}")
    private String senderNumber;

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final StringRedisTemplate redisTemplate;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
    }

    @Transactional
    public VerificationResponse sendVerificationCode(VerificationRequest request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        String authCode = generateAuthCode();

        try {
            sendSms(phoneNumber, authCode);
        } catch (NurigoMessageNotReceivedException e) {
            log.error("문자 발송 실패: phoneNumber={}, failedList={}", phoneNumber, e.getFailedMessageList(), e);
            return VerificationResponse.builder()
                    .success(false)
                    .message("문자 발송에 실패했습니다.")
                    .build();
        } catch (Exception e) {
            log.error("문자 발송 실패: phoneNumber={}", phoneNumber, e);
            return VerificationResponse.builder()
                    .success(false)
                    .message("문자 발송에 실패했습니다.")
                    .build();
        }

        String redisKey = REDIS_KEY_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(redisKey, authCode, java.time.Duration.ofSeconds(REDIS_TTL_SECONDS));

        log.info("인증번호 [{}] 발송 완료 (Redis TTL {}초)", authCode, REDIS_TTL_SECONDS);

        return VerificationResponse.builder()
                .success(true)
                .message("인증번호가 발송되었습니다.")
                .build();
    }

    private void sendSms(String to, String authCode) throws Exception {
        Message message = new Message();
        message.setFrom(senderNumber);
        message.setTo(to);
        message.setText("[로드맵 독서실]\n인증번호는 " + authCode + " 입니다.\n3분 내로 입력해 주세요!");
        messageService.send(message);
    }

    @Transactional
    public VerificationVerifyResponse verifyCode(VerificationVerifyRequest request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        String redisKey = REDIS_KEY_PREFIX + phoneNumber;

        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null || storedCode.isBlank()) {
            return VerificationVerifyResponse.builder()
                    .success(false)
                    .message("인증 요청을 찾을 수 없습니다. 먼저 인증번호 발송을 요청해주세요.")
                    .verificationToken(null)
                    .build();
        }

        if (!storedCode.equals(request.getAuthCode())) {
            return VerificationVerifyResponse.builder()
                    .success(false)
                    .message("인증번호가 일치하지 않습니다.")
                    .verificationToken(null)
                    .build();
        }

        redisTemplate.delete(redisKey);

        String token = UUID.randomUUID().toString();
        PhoneVerification phoneVerification = PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .authCode(request.getAuthCode())
                .isVerified(true)
                .verificationToken(token)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .createdAt(LocalDateTime.now())
                .build();
        phoneVerificationRepository.save(phoneVerification);

        return VerificationVerifyResponse.builder()
                .success(true)
                .message("인증이 완료되었습니다.")
                .verificationToken(token)
                .build();
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        int min = (int) Math.pow(10, AUTH_CODE_LENGTH - 1);
        int max = (int) Math.pow(10, AUTH_CODE_LENGTH) - 1;
        int code = min + random.nextInt(max - min + 1);
        return String.valueOf(code);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}
