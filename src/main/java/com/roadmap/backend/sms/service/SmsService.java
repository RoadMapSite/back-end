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
     * 기본 발신 번호({@code solapi.sender-number})로 SMS 발송.
     */
    public void send(String to, String text) {
        send(to, text, senderNumber);
    }

    /**
     * 지정 발신 번호로 SMS 발송. {@code from}이 비어 있으면 기본 발신 번호를 사용한다.
     */
    public void send(String to, String text, String from) {
        String normalizedTo = normalizePhoneNumber(to);
        if (normalizedTo.isBlank()) {
            log.warn("SMS 발송 스킵: 수신 번호가 비어있음");
            return;
        }
        String fromNumber = (from != null && !from.isBlank()) ? normalizePhoneNumber(from) : senderNumber;
        if (fromNumber == null || fromNumber.isBlank()) {
            log.warn("SMS 발송 스킵: 발신 번호가 비어있음");
            return;
        }
        try {
            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(normalizedTo);
            message.setText(text);
            message.setSubject("[로드맵 독서실]");
            messageService.send(message);
            log.info("SMS 발송 완료: from={}, to={}", fromNumber, normalizedTo);
        } catch (Exception e) {
            log.error("SMS 발송 실패: from={}, to={}", fromNumber, normalizedTo, e);
            throw new IllegalStateException("SMS 발송 실패", e);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}
