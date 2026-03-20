package com.roadmap.backend.sms.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.sender-number}")
    private String senderNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
    }

    /**
     * SMS 발송. 실패 시 로깅만 하고 예외를 던지지 않음 (비즈니스 로직 실패 방지).
     */
    public void send(String to, String text) {
        String normalizedTo = normalizePhoneNumber(to);
        if (normalizedTo.isBlank()) {
            log.warn("SMS 발송 스킵: 수신 번호가 비어있음");
            return;
        }
        try {
            Message message = new Message();
            message.setFrom(senderNumber);
            message.setTo(normalizedTo);
            message.setText(text);
            message.setSubject("[로드맵 독서실]");
            messageService.send(message);
            log.info("SMS 발송 완료: to={}", normalizedTo);
        } catch (Exception e) {
            log.error("SMS 발송 실패: to={}", normalizedTo, e);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}
